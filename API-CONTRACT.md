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

### 로그아웃

`POST /api/auth/logout`

```
Authorization: Bearer {accessToken}
```

```json
// Request
{
  "refreshToken": "eyJhbGciOi..."
}

// Response 204 No Content
```

> 서버에서 refresh token을 무효화합니다. WebSocket/SSE 연결이 있으면 해제하고, 친구들에게 오프라인 상태를 알립니다.

### 인증 헤더

모든 인증 필요 API에 다음 헤더를 포함합니다.

```
Authorization: Bearer {accessToken}
```

---

## 2. 사용자 / 친구 API

### 내 정보 조회

`GET /api/users/me`

```json
// Response 200
{
  "email": "hellu@example.com",
  "nickname": "헬루",
  "createdAt": "2026-03-18T12:00:00Z"
}
```

> 앱 시작 시 토큰 갱신 후 1회 호출하여 현재 사용자 정보를 확인합니다.

### 프로필 수정

`PUT /api/users/me`

```json
// Request
{
  "nickname": "새닉네임"
}

// Response 200
{
  "email": "hellu@example.com",
  "nickname": "새닉네임",
  "createdAt": "2026-03-18T12:00:00Z"
}
```

### 회원 탈퇴

`DELETE /api/users/me`

```json
// Response 204 No Content
```

> soft delete 처리 (`deletedAt` 설정). 활성 WebSocket/SSE 연결을 해제하고, 친구들에게 오프라인 상태를 알립니다.

### 사용자 검색 (이메일로)

`GET /api/users/search?email={email}`

```json
// Response 200
{
  "email": "friend@example.com",
  "nickname": "친구",
  "relationStatus": "NONE"
}
```

| relationStatus | 의미 | 클라이언트 동작 |
|---|---|---|
| `NONE` | 관계 없음 | 친구 요청 버튼 활성화 |
| `PENDING_SENT` | 내가 보낸 요청 대기 중 | "요청 보냄" 표시, 버튼 비활성화 |
| `PENDING_RECEIVED` | 상대가 보낸 요청 대기 중 | "수락/거절" 표시 |
| `FRIENDS` | 이미 친구 | "이미 친구" 표시, 버튼 숨김 |

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

### 보낸 친구 요청 목록

`GET /api/friends/requests/sent`

```json
// Response 200
[
  {
    "requestId": "uuid",
    "toEmail": "someone@example.com",
    "toNickname": "누군가",
    "createdAt": "2026-03-18T12:00:00Z"
  }
]
```

### 친구 요청 취소

`DELETE /api/friends/request/{requestId}`

```json
// Response 204 No Content
```

> PENDING 상태인 요청만 취소할 수 있습니다. 본인이 보낸 요청만 취소 가능합니다.

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

### 친구 삭제

`DELETE /api/friends/{friendEmail}`

```json
// Response 204 No Content
```

> Friendship 레코드를 삭제합니다. 양쪽 친구 목록에서 모두 제거됩니다.

---

## 3. 채팅 API

### 채팅방 생성 / 조회

`POST /api/chats`

```json
// Request
{
  "friendEmail": "friend@example.com"
}

// Response 200 (이미 존재하는 경우) / 201 (새로 생성)
{
  "chatId": "uuid",
  "friendEmail": "friend@example.com",
  "friendNickname": "친구",
  "createdAt": "2026-03-18T12:00:00Z"
}
```

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

`GET /api/chats/{chatId}/messages?before={timestamp}&after={timestamp}&limit=50`

| 파라미터 | 필수 | 설명 |
|----------|------|------|
| `before` | 선택 | 이 시각 이전 메시지 조회 (과거 스크롤용) |
| `after` | 선택 | 이 시각 이후 메시지 조회 (동기화용, 모바일 백그라운드 복귀 시) |
| `limit` | 선택 | 최대 건수 (기본 50) |

> `before`와 `after`는 동시에 사용할 수 없습니다. 둘 다 없으면 최신 메시지부터 반환합니다.

```json
// Response 200
[
  {
    "messageId": "uuid",
    "chatId": "uuid",
    "senderEmail": "hellu@example.com",
    "content": "안녕!",
    "timestamp": "2026-03-18T12:00:00Z",
    "read": true
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

#### 서버 → 클라이언트 (CHAT — 에코 포함)

```json
{
  "type": "CHAT",
  "chatId": "uuid",
  "messageId": "uuid",
  "senderEmail": "friend@example.com",
  "content": "메시지 내용",
  "timestamp": "2026-03-18T12:00:00Z"
}
```

> 송신자에게도 동일한 메시지를 에코합니다. (서버에서 생성된 `messageId`, `timestamp` 확정용)

#### 클라이언트 → 서버 (READ)

```json
{
  "type": "READ",
  "chatId": "uuid"
}
```

#### 서버 → 클라이언트 (READ)

```json
{
  "type": "READ",
  "chatId": "uuid",
  "readerEmail": "friend@example.com"
}
```

#### 클라이언트 → 서버 (TYPING)

```json
{
  "type": "TYPING",
  "chatId": "uuid"
}
```

#### 서버 → 클라이언트 (TYPING)

```json
{
  "type": "TYPING",
  "chatId": "uuid",
  "senderEmail": "friend@example.com"
}
```

#### 서버 → 클라이언트 (ONLINE)

```json
{
  "type": "ONLINE",
  "email": "friend@example.com",
  "online": true
}
```

### 메시지 type

| type | 방향 | 설명 |
|------|------|------|
| `CHAT` | 양방향 | 일반 채팅 메시지 (서버→클라이언트에 `messageId` 포함) |
| `READ` | 양방향 | 메시지 읽음 처리 |
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

event: FRIEND_ACCEPTED
data: {"email":"friend@example.com","nickname":"친구"}

event: FRIEND_ONLINE
data: {"email":"friend@example.com","online":true}

event: NEW_MESSAGE
data: {"chatId":"uuid","senderNickname":"친구","preview":"안녕!"}
```

| event | 설명 |
|-------|------|
| `FRIEND_REQUEST` | 새 친구 요청 수신 |
| `FRIEND_ACCEPTED` | 내가 보낸 친구 요청이 수락됨 |
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
| `INVALID_TOKEN` | 401 | 유효하지 않은 토큰 (변조, 형식 오류) |
| `TOKEN_EXPIRED` | 401 | 토큰 만료 |
| `EMAIL_ALREADY_EXISTS` | 409 | 이미 가입된 이메일 |
| `USER_NOT_FOUND` | 404 | 사용자 없음 |
| `SELF_FRIEND_REQUEST` | 400 | 자기 자신에게 친구 요청 |
| `PENDING_REQUEST_EXISTS` | 409 | 이미 대기 중인 친구 요청 존재 |
| `ALREADY_FRIENDS` | 409 | 이미 친구 상태 |
| `NOT_FRIENDS` | 403 | 친구가 아닌 사용자와 채팅 시도 |
| `FRIEND_REQUEST_NOT_FOUND` | 404 | 존재하지 않는 친구 요청 |
| `CHAT_NOT_FOUND` | 404 | 존재하지 않는 채팅방 |
