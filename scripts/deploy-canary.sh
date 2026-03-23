#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="docker-compose.yml"
NGINX_CONF="nginx/nginx.conf"
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$PROJECT_DIR"

usage() {
    echo "Usage: $0 {start|scale <percent>|promote|rollback}"
    echo ""
    echo "Commands:"
    echo "  start          - canary 컨테이너 시작, 트래픽 10%"
    echo "  scale <percent> - canary 트래픽 비율 변경 (1-99)"
    echo "  promote        - canary를 stable로 승격 (100%)"
    echo "  rollback       - canary 중단, stable 100% 복원"
    exit 1
}

# nginx.conf의 upstream weight를 변경
set_weights() {
    local stable_weight=$1
    local canary_weight=$2

    if [ "$canary_weight" -eq 0 ]; then
        # canary 비활성: canary 라인 주석 처리
        sed -i.bak \
            -e '/CANARY_CONFIG_START/,/CANARY_CONFIG_END/{
                s|.*server stable:8080.*|    server stable:8080 weight='"$stable_weight"';|
                s|.*server canary:8080.*|    # server canary:8080 weight=0;|
            }' "$NGINX_CONF"
    else
        # canary 활성: 주석 해제
        sed -i.bak \
            -e '/CANARY_CONFIG_START/,/CANARY_CONFIG_END/{
                s|.*server stable:8080.*|    server stable:8080 weight='"$stable_weight"';|
                s|.*server canary:8080.*|    server canary:8080 weight='"$canary_weight"';|
            }' "$NGINX_CONF"
    fi
    rm -f "${NGINX_CONF}.bak"
}

reload_nginx() {
    docker compose exec nginx nginx -s reload
    echo "Nginx 설정 리로드 완료"
}

cmd_start() {
    echo "==> canary 컨테이너 빌드 및 시작..."
    docker compose --profile canary up -d --build canary

    echo "==> 트래픽 비율: stable 90% / canary 10%"
    set_weights 9 1
    reload_nginx

    echo "카나리아 배포 시작됨 (10%)"
}

cmd_scale() {
    local percent=${1:-}
    if [ -z "$percent" ] || [ "$percent" -lt 1 ] || [ "$percent" -gt 99 ]; then
        echo "Error: 1~99 사이의 비율을 입력하세요"
        exit 1
    fi

    local stable_weight=$((100 - percent))
    local canary_weight=$percent

    echo "==> 트래픽 비율: stable ${stable_weight}% / canary ${canary_weight}%"
    set_weights "$stable_weight" "$canary_weight"
    reload_nginx

    echo "트래픽 비율 변경 완료"
}

cmd_promote() {
    echo "==> canary를 stable로 승격..."

    # 1. canary 이미지를 stable로 교체
    docker compose --profile canary stop canary
    docker compose up -d --build stable

    # 2. 트래픽 stable 100%로 복원
    set_weights 10 0
    reload_nginx

    # 3. canary 컨테이너 제거
    docker compose --profile canary rm -f canary

    echo "승격 완료: stable이 새 버전으로 교체됨"
}

cmd_rollback() {
    echo "==> 카나리아 롤백..."

    # 1. 트래픽 stable 100%로 복원
    set_weights 10 0
    reload_nginx

    # 2. canary 컨테이너 중단 및 제거
    docker compose --profile canary stop canary
    docker compose --profile canary rm -f canary

    echo "롤백 완료: stable 100%"
}

case "${1:-}" in
    start)    cmd_start ;;
    scale)    cmd_scale "${2:-}" ;;
    promote)  cmd_promote ;;
    rollback) cmd_rollback ;;
    *)        usage ;;
esac
