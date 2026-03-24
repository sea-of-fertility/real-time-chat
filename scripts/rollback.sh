#!/bin/bash
set -euo pipefail
source /root/app/scripts/deploy-common.sh

ensure_state_dir

ACTIVE=$(get_active_color)
INACTIVE=$(get_inactive_color)

echo "=== 롤백 ==="
echo "현재 활성: $ACTIVE"

# 비활성 컨테이너가 실행 중인지 확인
INACTIVE_STATUS=$(docker inspect --format='{{.State.Status}}' "app-${INACTIVE}" 2>/dev/null || echo "not_found")

if [ "$INACTIVE_STATUS" = "running" ]; then
    # 이전 컨테이너가 아직 실행 중 → upstream만 전환
    echo "app-${INACTIVE}이 실행 중. 트래픽 전환."
    cp "/root/app/nginx/upstream-${INACTIVE}.conf" "$NGINX_CONF_DIR/upstream.conf"
    reload_nginx
    set_active_color "$INACTIVE"
    echo "롤백 완료. 현재 활성: $INACTIVE"
else
    # 비활성 컨테이너가 중지됨 → 이전 이미지로 재시작
    PREV_IMAGE=$(cat "$STATE_DIR/previous-image" 2>/dev/null || echo "")
    if [ -z "$PREV_IMAGE" ]; then
        echo "ERROR: 이전 이미지 정보 없음. 수동 복구 필요."
        exit 1
    fi

    echo "app-${INACTIVE}을 이전 이미지로 시작: $PREV_IMAGE"

    COLOR_VAR=$(echo "${INACTIVE}" | tr '[:lower:]' '[:upper:]')
    sed -i "s|^${COLOR_VAR}_IMAGE=.*|${COLOR_VAR}_IMAGE=${PREV_IMAGE}|" "$COMPOSE_DIR/.env" || \
        echo "${COLOR_VAR}_IMAGE=${PREV_IMAGE}" >> "$COMPOSE_DIR/.env"

    docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d "app-${INACTIVE}"

    if wait_for_healthy "app-${INACTIVE}" 120; then
        cp "/root/app/nginx/upstream-${INACTIVE}.conf" "$NGINX_CONF_DIR/upstream.conf"
        reload_nginx
        set_active_color "$INACTIVE"
        echo "롤백 완료. 현재 활성: $INACTIVE"
    else
        echo "ERROR: 롤백 실패. 이전 버전도 정상 기동 불가."
        exit 1
    fi
fi
