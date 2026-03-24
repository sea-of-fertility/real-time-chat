#!/bin/bash
set -euo pipefail

STATE_DIR="/root/app/state"
COMPOSE_DIR="/root/app"
NGINX_CONF_DIR="/root/app/nginx/conf.d"

# 상태 디렉토리 초기화
ensure_state_dir() {
    mkdir -p "$STATE_DIR"
    mkdir -p "$NGINX_CONF_DIR"
    if [ ! -f "$STATE_DIR/active-color" ]; then
        echo "blue" > "$STATE_DIR/active-color"
    fi
}

# 현재 활성 색상 조회
get_active_color() {
    cat "$STATE_DIR/active-color"
}

# 비활성 색상 조회
get_inactive_color() {
    local active
    active=$(get_active_color)
    if [ "$active" = "blue" ]; then
        echo "green"
    else
        echo "blue"
    fi
}

# 활성 색상 변경
set_active_color() {
    echo "$1" > "$STATE_DIR/active-color"
}

# 롤백용 이전 이미지 저장
save_previous_image() {
    local color="$1"
    docker inspect --format='{{.Config.Image}}' "app-${color}" 2>/dev/null > "$STATE_DIR/previous-image" || true
}

# 컨테이너 헬스체크 대기
# 인자: container_name, max_wait_seconds
wait_for_healthy() {
    local container="$1"
    local max_wait="${2:-120}"
    local elapsed=0

    echo "[$container] 헬스체크 대기 중 (최대 ${max_wait}초)..."
    while [ $elapsed -lt $max_wait ]; do
        local status
        status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "not_found")
        if [ "$status" = "healthy" ]; then
            echo "[$container] 정상 (${elapsed}초 경과)"
            return 0
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done

    echo "ERROR: [$container] ${max_wait}초 내에 정상 상태가 되지 않았습니다."
    return 1
}

# Nginx 설정 검증 후 리로드
reload_nginx() {
    docker exec nginx nginx -t && docker exec nginx nginx -s reload
    echo "Nginx 리로드 완료"
}

# upstream 설정 적용
apply_upstream() {
    local source="$1"
    cp "$source" "$NGINX_CONF_DIR/upstream.conf"
    reload_nginx
}
