#!/bin/bash
set -euo pipefail
source /root/app/scripts/deploy-common.sh

IMAGE="$1"

ensure_state_dir

ACTIVE=$(get_active_color)
INACTIVE=$(get_inactive_color)

echo "=== Blue-Green 배포 ==="
echo "현재 활성: $ACTIVE | 배포 대상: $INACTIVE"
echo "이미지: $IMAGE"

# 롤백용 이전 이미지 저장
save_previous_image "$ACTIVE"

# 비활성 컨테이너에 새 이미지 변수 설정
COLOR_VAR=$(echo "${INACTIVE}" | tr '[:lower:]' '[:upper:]')
sed -i "s|^${COLOR_VAR}_IMAGE=.*|${COLOR_VAR}_IMAGE=${IMAGE}|" "$COMPOSE_DIR/.env" || \
    echo "${COLOR_VAR}_IMAGE=${IMAGE}" >> "$COMPOSE_DIR/.env"

# 비활성 컨테이너 시작
docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d "app-${INACTIVE}"

# 헬스체크 대기
if ! wait_for_healthy "app-${INACTIVE}" 120; then
    echo "헬스체크 실패. 비활성 컨테이너 중지."
    docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop "app-${INACTIVE}"
    exit 1
fi

# 트래픽 전환
cp "/root/app/nginx/upstream-${INACTIVE}.conf" "$NGINX_CONF_DIR/upstream.conf"
reload_nginx

echo "트래픽이 $INACTIVE 로 전환되었습니다."

# 상태 업데이트
set_active_color "$INACTIVE"

# 이전 컨테이너 중지 (롤백 시 재시작 필요)
docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop "app-${ACTIVE}"

echo "=== Blue-Green 배포 완료 ==="
echo "현재 활성: $INACTIVE"
