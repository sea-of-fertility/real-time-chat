# API Contract — Backend ↔ Frontend

> Backend: Spring WebFlux (Java)
> Frontend: Next.js (TypeScript)

---

## 1. 인증 API

### 회원가입

`POST /api/auth/signup`

```json
// Request
{
  "email": "hellu@example.com",
  "password": "password123",
  "nickname": "헬루"
}

// Response 201
{
  "email": "hellu@example.com",
  "nickname": "헬루",
  "createdAt": "2026-03-18T12:00:00Z"
}
```

### 로그인

`POST /api/auth/login`

```json
// Request
{
  "email": "hellu@example.com",
  "password": "password123"
}

// Response 200
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi..."
}
```

### 토큰 갱신

`POST /api/auth/refresh`

```json
// Request
{
  "refreshToken": "eyJhbGciOi..."
}

// Response 200
{
  "accessToken": "eyJhbGciOi..."
}
```

### 인증 헤더

모든 인증 필요 API에 다음 헤더를 포함합니다.

```
Authorization: Bearer {accessToken}
```

---

## 2. 사용자 / 친구 API

### 사용자 검색 (이메일로)

`GET /api/users/search?email={email}`

```json
// Response 200
{
  "email": "friend@example.com",
  "nickname": "친구"
}
```

### 친구 요청 보내기

`POST /api/friends/request`

```json
// Request
{
  "targetEmail": "friend@example.com"
}

// Response 201
{
  "requestId": "uuid",
  "status": "PENDING"
}
```

### 받은 친구 요청 목록

`GET /api/friends/requests/received`

```json
// Response 200
[
  {
    "requestId": "uuid",
    "fromEmail": "someone@example.com",
    "fromNickname": "누군가",
    "createdAt": "2026-03-18T12:00:00Z"
  }
]
```

### 친구 요청 수락 / 거절

`PUT /api/friends/request/{requestId}`

```json
// Request
{
  "action": "ACCEPT"  // or "REJECT"
}

// Response 200
{
  "status": "ACCEPTED"
}
```

### 친구 목록 조회

`GET /api/friends`

```json
// Response 200
[
  {
    "email": "friend@example.com",
    "nickname": "친구",
    "online": true
  }
]
```

---

## 3. 채팅 API

### 채팅방 목록 (내 1:1 채팅방들)

`GET /api/chats`

```json
// Response 200
[
  {
    "chatId": "uuid",
    "friendEmail": "friend@example.com",
    "friendNickname": "친구",
    "lastMessage": "안녕!",
    "lastMessageAt": "2026-03-18T12:00:00Z",
    "unreadCount": 3
  }
]
```

### 메시지 히스토리

`GET /api/chats/{chatId}/messages?before={timestamp}&limit=50`

```json
// Response 200
[
  {
    "messageId": "uuid",
    "senderEmail": "hellu@example.com",
    "content": "안녕!",
    "timestamp": "2026-03-18T12:00:00Z"
  }
]
```

---

## 4. WebSocket

### 연결

```
ws(s)://{host}/ws/chat?token={accessToken}
```

JWT 토큰을 쿼리 파라미터로 전달하여 인증합니다.

### 메시지 포맷

#### 클라이언트 → 서버

```json
{
  "type": "CHAT",
  "chatId": "uuid",
  "content": "메시지 내용"
}
```

#### 서버 → 클라이언트

```json
{
  "type": "CHAT",
  "chatId": "uuid",
  "senderEmail": "friend@example.com",
  "content": "메시지 내용",
  "timestamp": "2026-03-18T12:00:00Z"
}
```

### 메시지 type

| type | 방향 | 설명 |
|------|------|------|
| `CHAT` | 양방향 | 일반 채팅 메시지 |
| `READ` | 클라이언트→서버 | 메시지 읽음 처리 |
| `TYPING` | 양방향 | 상대방 입력 중 표시 |
| `ONLINE` | 서버→클라이언트 | 친구 온라인/오프라인 상태 변경 |

---

## 5. SSE — 알림

### 연결

`GET /sse/notifications`

```
Authorization: Bearer {accessToken}
```

**Content-Type**: `text/event-stream`

### 이벤트

```
event: FRIEND_REQUEST
data: {"fromEmail":"someone@example.com","fromNickname":"누군가"}

event: FRIEND_ONLINE
data: {"email":"friend@example.com","online":true}

event: NEW_MESSAGE
data: {"chatId":"uuid","senderNickname":"친구","preview":"안녕!"}
```

| event | 설명 |
|-------|------|
| `FRIEND_REQUEST` | 새 친구 요청 수신 |
| `FRIEND_ONLINE` | 친구 온라인/오프라인 변경 |
| `NEW_MESSAGE` | 새 메시지 도착 (WebSocket 미연결 시 폴백) |

---

## 6. CORS 설정

| 환경 | 허용 Origin |
|------|-------------|
| 로컬 개발 | `http://localhost:3000` |
| 배포 | 배포 도메인 (추후 결정) |

---

## 7. 공통 규칙

- 모든 JSON 필드는 **camelCase**
- 시간은 **ISO 8601 (UTC)** 형식 (`2026-03-18T12:00:00Z`)
- 인증은 **JWT** (Access Token + Refresh Token)
- 사용자 고유 식별자는 **이메일**
- WebSocket 연결 시 JWT를 쿼리 파라미터로 전달
- 에러 응답 포맷:

```json
{
  "code": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다."
}
```

### 주요 에러 코드

| code | HTTP Status | 설명 |
|------|-------------|------|
| `INVALID_CREDENTIALS` | 401 | 이메일/비밀번호 불일치 |
| `EMAIL_ALREADY_EXISTS` | 409 | 이미 가입된 이메일 |
| `USER_NOT_FOUND` | 404 | 사용자 없음 |
| `ALREADY_FRIENDS` | 409 | 이미 친구 상태 |
| `TOKEN_EXPIRED` | 401 | 토큰 만료 |
