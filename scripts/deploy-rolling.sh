#!/bin/bash
set -euo pipefail
source /root/app/scripts/deploy-common.sh

IMAGE="$1"

ensure_state_dir

ACTIVE=$(get_active_color)
INACTIVE=$(get_inactive_color)

echo "=== Rolling 배포 ==="
echo "이미지: $IMAGE"

# 두 컨테이너 모두 이미지 업데이트 대상
# .env에 양쪽 이미지 변수 설정
sed -i "s|^BLUE_IMAGE=.*|BLUE_IMAGE=${IMAGE}|" "$COMPOSE_DIR/.env" || \
    echo "BLUE_IMAGE=${IMAGE}" >> "$COMPOSE_DIR/.env"
sed -i "s|^GREEN_IMAGE=.*|GREEN_IMAGE=${IMAGE}|" "$COMPOSE_DIR/.env" || \
    echo "GREEN_IMAGE=${IMAGE}" >> "$COMPOSE_DIR/.env"

# 롤백용 이전 이미지 저장
save_previous_image "$ACTIVE"

# Step 1: 양쪽 모두 실행 + upstream에 등록
docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d --force-recreate "app-${ACTIVE}"

cat > "$NGINX_CONF_DIR/upstream.conf" <<EOF
upstream chat_backend {
    server app-blue:8080;
    server app-green:8080;
}
EOF
reload_nginx

# Step 2: 비활성 컨테이너 먼저 업데이트
echo "--- Rolling: app-${INACTIVE} 업데이트 ---"
docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop "app-${INACTIVE}"

docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d --force-recreate "app-${INACTIVE}"

if ! wait_for_healthy "app-${INACTIVE}" 120; then
    echo "app-${INACTIVE} 헬스체크 실패. 단일 서버로 유지."
    cp "/root/app/nginx/upstream-${ACTIVE}.conf" "$NGINX_CONF_DIR/upstream.conf"
    reload_nginx
    exit 1
fi

echo "app-${INACTIVE} 업데이트 완료"

# upstream에 다시 추가
cat > "$NGINX_CONF_DIR/upstream.conf" <<EOF
upstream chat_backend {
    server app-blue:8080;
    server app-green:8080;
}
EOF
reload_nginx

# Step 3: 활성 컨테이너 업데이트
echo "--- Rolling: app-${ACTIVE} 업데이트 ---"
docker compose -f "$COMPOSE_DIR/docker-compose.yml" stop "app-${ACTIVE}"

docker compose -f "$COMPOSE_DIR/docker-compose.yml" up -d --force-recreate "app-${ACTIVE}"

if ! wait_for_healthy "app-${ACTIVE}" 120; then
    echo "app-${ACTIVE} 헬스체크 실패. app-${INACTIVE}만 서비스."
    cp "/root/app/nginx/upstream-${INACTIVE}.conf" "$NGINX_CONF_DIR/upstream.conf"
    reload_nginx
    set_active_color "$INACTIVE"
    exit 1
fi

echo "app-${ACTIVE} 업데이트 완료"

# 양쪽 모두 새 이미지로 서비스
cat > "$NGINX_CONF_DIR/upstream.conf" <<EOF
upstream chat_backend {
    server app-blue:8080;
    server app-green:8080;
}
EOF
reload_nginx

echo "=== Rolling 배포 완료 ==="
echo "app-blue, app-green 모두 $IMAGE 실행 중"
