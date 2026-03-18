# Frontend Endpoint Reference

> 프론트엔드(Next.js) 개발 시 참조용 엔드포인트 요약
> 상세 스펙은 [API-CONTRACT.md](./API-CONTRACT.md) 참조

---

## Base URL

| 환경 | REST / SSE | WebSocket |
|------|-----------|-----------|
| 로컬 | `http://localhost:8080` | `ws://localhost:8080` |
| 배포 | 추후 결정 | 추후 결정 |

---

## 인증 (Public — 토큰 불필요)

| Method | Endpoint | 설명 | Request Body | Response |
|--------|----------|------|-------------|----------|
| `POST` | `/api/auth/signup` | 회원가입 | `{ email, password, nickname }` | `{ email, nickname, createdAt }` |
| `POST` | `/api/auth/login` | 로그인 | `{ email, password }` | `{ accessToken, refreshToken }` |
| `POST` | `/api/auth/refresh` | 토큰 갱신 | `{ refreshToken }` | `{ accessToken }` |

---

## 사용자 / 친구 (Protected — `Authorization: Bearer {token}`)

| Method | Endpoint | 설명 | Request | Response |
|--------|----------|------|---------|----------|
| `GET` | `/api/users/search?email={email}` | 사용자 검색 | Query param | `{ email, nickname }` |
| `POST` | `/api/friends/request` | 친구 요청 | `{ targetEmail }` | `{ requestId, status }` |
| `GET` | `/api/friends/requests/received` | 받은 요청 목록 | — | `[{ requestId, fromEmail, fromNickname, createdAt }]` |
| `PUT` | `/api/friends/request/{requestId}` | 요청 수락/거절 | `{ action: "ACCEPT" \| "REJECT" }` | `{ status }` |
| `GET` | `/api/friends` | 친구 목록 | — | `[{ email, nickname, online }]` |

---

## 채팅 (Protected)

| Method | Endpoint | 설명 | Request | Response |
|--------|----------|------|---------|----------|
| `GET` | `/api/chats` | 내 채팅방 목록 | — | `[{ chatId, friendEmail, friendNickname, lastMessage, lastMessageAt, unreadCount }]` |
| `GET` | `/api/chats/{chatId}/messages?before={ISO}&limit=50` | 메시지 히스토리 | Query params | `[{ messageId, senderEmail, content, timestamp }]` |

---

## WebSocket

**연결**: `ws(s)://{host}/ws/chat?token={accessToken}`

### 클라이언트 → 서버

| type | 필드 | 설명 |
|------|------|------|
| `CHAT` | `chatId`, `content` | 메시지 전송 |
| `READ` | `chatId` | 읽음 처리 |
| `TYPING` | `chatId` | 입력 중 알림 |

### 서버 → 클라이언트

| type | 필드 | 설명 |
|------|------|------|
| `CHAT` | `chatId`, `senderEmail`, `content`, `timestamp` | 메시지 수신 |
| `TYPING` | `chatId`, `senderEmail` | 상대방 입력 중 |
| `ONLINE` | `email`, `online` | 온라인 상태 변경 |

---

## SSE

**연결**: `GET /sse/notifications` (Header: `Authorization: Bearer {token}`)

| event | data 필드 | 설명 |
|-------|----------|------|
| `FRIEND_REQUEST` | `fromEmail`, `fromNickname` | 새 친구 요청 |
| `FRIEND_ONLINE` | `email`, `online` | 친구 온/오프라인 |
| `NEW_MESSAGE` | `chatId`, `senderNickname`, `preview` | 새 메시지 (WS 미연결 시) |

---

## 에러 응답 포맷

```json
{
  "code": "ERROR_CODE",
  "message": "사용자에게 보여줄 메시지"
}
```

| code | Status | 설명 |
|------|--------|------|
| `INVALID_CREDENTIALS` | 401 | 이메일/비밀번호 불일치 |
| `EMAIL_ALREADY_EXISTS` | 409 | 이미 가입된 이메일 |
| `USER_NOT_FOUND` | 404 | 사용자 없음 |
| `ALREADY_FRIENDS` | 409 | 이미 친구 상태 |
| `TOKEN_EXPIRED` | 401 | 토큰 만료 |

---

## 공통 규칙 요약

- JSON 필드: **camelCase**
- 시간: **ISO 8601 UTC** (`2026-03-18T12:00:00Z`)
- 인증: **JWT Bearer Token** (모든 Protected API)
- 사용자 식별자: **이메일**
