#!/usr/bin/env bash
set -euo pipefail

NGINX_CONF="nginx/bluegreen.conf"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$PROJECT_DIR"

usage() {
    echo "Usage: $0 {status|deploy|switch|rollback}"
    echo ""
    echo "Commands:"
    echo "  status   - 현재 활성 환경 확인 (blue/green)"
    echo "  deploy   - 비활성 환경에 새 버전 배포 후 전환"
    echo "  switch   - 트래픽을 반대쪽 환경으로 전환"
    echo "  rollback - 이전 환경으로 되돌리기"
    exit 1
}

# 현재 활성 환경 읽기
get_active() {
    grep "# BLUEGREEN_ACTIVE:" "$NGINX_CONF" | awk '{print $3}'
}

get_inactive() {
    local active
    active=$(get_active)
    if [ "$active" = "blue" ]; then
        echo "green"
    else
        echo "blue"
    fi
}

# nginx.conf upstream을 대상 환경으로 변경
switch_to() {
    local target=$1
    sed -i.bak \
        -e "s|# BLUEGREEN_ACTIVE: .*|# BLUEGREEN_ACTIVE: $target|" \
        -e "s|server .*:8080;|server $target:8080;|" \
        "$NGINX_CONF"
    rm -f "${NGINX_CONF}.bak"
}

reload_nginx() {
    docker compose exec nginx nginx -s reload
    echo "Nginx 설정 리로드 완료"
}

cmd_status() {
    local active
    active=$(get_active)
    local inactive
    inactive=$(get_inactive)
    echo "활성: $active"
    echo "대기: $inactive"
}

cmd_deploy() {
    local active
    active=$(get_active)
    local inactive
    inactive=$(get_inactive)

    echo "==> 현재 활성: $active"
    echo "==> $inactive 환경에 새 버전 빌드 및 배포..."
    docker compose --profile bluegreen up -d --build "$inactive"

    echo "==> $inactive 헬스체크 대기..."
    local retries=30
    while [ $retries -gt 0 ]; do
        if docker compose --profile bluegreen exec "$inactive" curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
            echo "==> $inactive 정상 확인"
            break
        fi
        retries=$((retries - 1))
        sleep 2
    done

    if [ $retries -eq 0 ]; then
        echo "WARNING: 헬스체크 타임아웃. 수동 확인 필요"
        echo "전환하려면: $0 switch"
        exit 1
    fi

    echo "==> 트래픽을 $inactive 으로 전환..."
    switch_to "$inactive"
    reload_nginx

    echo "배포 완료: $inactive 활성화됨"
}

cmd_switch() {
    local active
    active=$(get_active)
    local inactive
    inactive=$(get_inactive)

    echo "==> $active → $inactive 으로 트래픽 전환..."
    switch_to "$inactive"
    reload_nginx

    echo "전환 완료: $inactive 활성화됨"
}

cmd_rollback() {
    # rollback = switch (이전 환경으로 다시 전환)
    echo "==> 롤백 실행 (이전 환경으로 전환)..."
    cmd_switch
}

case "${1:-}" in
    status)   cmd_status ;;
    deploy)   cmd_deploy ;;
    switch)   cmd_switch ;;
    rollback) cmd_rollback ;;
    *)        usage ;;
esac
