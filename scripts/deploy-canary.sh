#!/bin/bash
set -euo pipefail
source /root/app/scripts/deploy-common.sh

IMAGE="$1"
CANARY_STEPS="${2:-10,50,100}"

ensure_state_dir

ACTIVE=$(get_active_color)
INACTIVE=$(get_inactive_color)

echo "=== Canary 배포 ==="
echo "현재 활성: $ACTIVE | 카나리: $INACTIVE"
echo "이미지: $IMAGE"
echo "가중치 단계: $CANARY_STEPS"

# 롤백용 이전 이미지 저장
save_previous_image "$ACTIVE"

# 카나리 컨테이너에 새 이미지 설정
COLOR_VAR=$(echo "${INACTIVE}" | tr '[:lower:]' '[:upper:]')
sed -i "s|^${COLOR_VAR}_IMAGE=.*|${COLOR_VAR}_IMAGE=${IMAGE}|" "$COMPOSE_DIR/.env" || \
    echo "${COLOR_VAR}_IMAGE=${IMAGE}" >> "$COMPOSE_DIR/.env"

# 카나리 컨테이너를 새 이미지로 재생성
docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d --force-recreate "app-${INACTIVE}"

# 헬스체크 대기
if ! wait_for_healthy "app-${INACTIVE}" 120; then
    echo "카나리 헬스체크 실패. 카나리 중지."
    docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop "app-${INACTIVE}"
    exit 1
fi

# 가중치 단계별 전환
IFS=',' read -ra STEPS <<< "$CANARY_STEPS"
for step in "${STEPS[@]}"; do
    new_weight=$step
    old_weight=$((100 - new_weight))

    echo "--- 카나리 단계: $INACTIVE ${new_weight}% / $ACTIVE ${old_weight}% ---"

    # 가중치에 따른 upstream 설정 생성
    if [ "$ACTIVE" = "blue" ]; then
        BLUE_WEIGHT=$old_weight
        GREEN_WEIGHT=$new_weight
    else
        BLUE_WEIGHT=$new_weight
        GREEN_WEIGHT=$old_weight
    fi

    if [ "$BLUE_WEIGHT" -eq 0 ]; then
        echo "upstream chat_backend { server app-green:8080; }" > "$NGINX_CONF_DIR/upstream.conf"
    elif [ "$GREEN_WEIGHT" -eq 0 ]; then
        echo "upstream chat_backend { server app-blue:8080; }" > "$NGINX_CONF_DIR/upstream.conf"
    else
        cat > "$NGINX_CONF_DIR/upstream.conf" <<EOF
upstream chat_backend {
    server app-blue:8080 weight=${BLUE_WEIGHT};
    server app-green:8080 weight=${GREEN_WEIGHT};
}
EOF
    fi

    reload_nginx

    # 마지막 단계(100%)가 아니면 대기 후 헬스 재확인
    if [ "$new_weight" -lt 100 ]; then
        echo "카나리 ${new_weight}% 상태. 60초 대기 후 다음 단계..."
        sleep 60

        local_status=$(docker inspect --format='{{.State.Health.Status}}' "app-${INACTIVE}" 2>/dev/null || echo "not_found")
        if [ "$local_status" != "healthy" ]; then
            echo "카나리 비정상 감지. 롤백 수행."
            cp "/root/app/nginx/upstream-${ACTIVE}.conf" "$NGINX_CONF_DIR/upstream.conf"
            reload_nginx
            docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop "app-${INACTIVE}"
            exit 1
        fi
    fi
done

# 전환 완료
set_active_color "$INACTIVE"

# 이전 컨테이너 중지
docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop "app-${ACTIVE}"

echo "=== Canary 배포 완료 ==="
echo "현재 활성: $INACTIVE (100% 트래픽)"
