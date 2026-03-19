# CLAUDE.md — 프로젝트 컨텍스트

## 프로젝트 개요

WebSocket + SSE 기반 1:1 실시간 채팅 백엔드 애플리케이션.
Spring WebFlux 리액티브 프로그래밍 학습 목적 프로젝트.

---

## 기술 스택 & 환경

| 항목 | 값 |
|------|----|
| Language | Java 25 |
| Framework | Spring Boot 4.0.3 (WebFlux) |
| Build | Gradle |
| DB (개발) | H2 인메모리 (R2DBC) |
| DB (배포) | PostgreSQL (R2DBC) |
| 인증 | JWT (Access Token 30분 / Refresh Token 7일) |
| 실시간 통신 | WebSocket (채팅) + SSE (알림) |
| 프론트엔드 | Next.js (별도 프로젝트, localhost:3000) |
| 서버 포트 | 8080 |
| 어노테이션 | Lombok 사용 |

---

## 패키지 구조

```
com.study.realtimechat/
├── config/          # WebSocketConfig, CorsConfig, SecurityConfig, R2dbcConfig
├── security/        # JwtProvider, JwtAuthenticationFilter
├── controller/      # AuthController, UserController, FriendController, ChatController, SseController
├── handler/         # ChatWebSocketHandler
├── service/         # AuthService, UserService, FriendService, ChatService, NotificationService
├── repository/      # UserRepository, FriendRequestRepository, FriendshipRepository, ChatRepository, MessageRepository
├── model/
│   ├── entity/      # User, FriendRequest, Friendship, Chat, Message
│   ├── dto/         # 각 API의 Request/Response DTO
│   └── enums/       # FriendRequestStatus, MessageType
└── exception/       # GlobalExceptionHandler, CustomException, ErrorCode
```

---

## 도메인 모델

### User
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK, auto increment |
| email | String | 고유 식별자, unique |
| password | String | BCrypt 해시 |
| nickname | String | 표시 이름 |
| createdAt | Instant | 가입 시각 |
| updatedAt | Instant | 수정 시각 |
| deletedAt | Instant | 삭제 시각 |

### FriendRequest
| 필드 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| fromEmail | String | 요청 보낸 사용자 |
| toEmail | String | 요청 받은 사용자 |
| status | Enum | PENDING, ACCEPTED, REJECTED |
| createdAt | Instant | 요청 시각 |

### Friendship (수락 시 생성)
| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| userEmail1 | String | 사용자 1 (정렬된 순서) |
| userEmail2 | String | 사용자 2 |
| createdAt | Instant | 친구 된 시각 |

### Chat
| 필드 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK (= chatId) |
| userEmail1 | String | 참여자 1 |
| userEmail2 | String | 참여자 2 |
| createdAt | Instant | 생성 시각 |

### Message
| 필드 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| chatId | UUID | FK → Chat |
| senderEmail | String | 보낸 사람 |
| content | String | 메시지 내용 |
| timestamp | Instant | 전송 시각 |
| read | Boolean | 읽음 여부 |

---

## API 엔드포인트

### 인증 (Public — 토큰 불필요)

| Method | Endpoint | 설명 | Request Body | Response |
|--------|----------|------|-------------|----------|
| POST | `/api/auth/signup` | 회원가입 | `{ email, password, nickname }` | `201 { email, nickname, createdAt }` |
| POST | `/api/auth/login` | 로그인 | `{ email, password }` | `200 { accessToken, refreshToken }` |
| POST | `/api/auth/refresh` | 토큰 갱신 | `{ refreshToken }` | `200 { accessToken }` |

### 사용자 / 친구 (Protected — `Authorization: Bearer {token}`)

| Method | Endpoint | 설명 | Request | Response |
|--------|----------|------|---------|----------|
| GET | `/api/users/search?email={email}` | 사용자 검색 | Query param | `{ email, nickname, relationStatus }` |
| POST | `/api/friends/request` | 친구 요청 | `{ targetEmail }` | `201 { requestId, status }` |
| GET | `/api/friends/requests/received` | 받은 요청 목록 | — | `[{ requestId, fromEmail, fromNickname, createdAt }]` |
| PUT | `/api/friends/request/{requestId}` | 수락/거절 | `{ action: "ACCEPT" \| "REJECT" }` | `{ status }` |
| GET | `/api/friends` | 친구 목록 | — | `[{ email, nickname, online }]` |

### 채팅 (Protected)

| Method | Endpoint | 설명 | Request | Response |
|--------|----------|------|---------|----------|
| POST | `/api/chats` | 채팅방 생성/조회 | `{ friendEmail }` | `200/201 { chatId, friendEmail, friendNickname, createdAt }` |
| GET | `/api/chats` | 내 채팅방 목록 | — | `[{ chatId, friendEmail, friendNickname, lastMessage, lastMessageAt, unreadCount }]` |
| GET | `/api/chats/{chatId}/messages?before={ISO}&limit=50` | 메시지 히스토리 (커서 기반 페이지네이션) | Query params | `[{ messageId, senderEmail, content, timestamp }]` |

### WebSocket

**연결**: `ws(s)://{host}/ws/chat?token={accessToken}`

클라이언트 → 서버:
```json
{ "type": "CHAT",   "chatId": "uuid", "content": "메시지" }
{ "type": "READ",   "chatId": "uuid" }
{ "type": "TYPING", "chatId": "uuid" }
```

서버 → 클라이언트:
```json
{ "type": "CHAT",   "chatId": "uuid", "messageId": "uuid", "senderEmail": "...", "content": "...", "timestamp": "..." }
{ "type": "READ",   "chatId": "uuid", "readerEmail": "..." }
{ "type": "TYPING", "chatId": "uuid", "senderEmail": "..." }
{ "type": "ONLINE", "email": "...", "online": true }
```

> CHAT 메시지는 송신자에게도 에코 (서버에서 생성된 `messageId`, `timestamp` 확정용)

### SSE

**연결**: `GET /sse/notifications` (Header: `Authorization: Bearer {token}`)

| event | data | 설명 |
|-------|------|------|
| FRIEND_REQUEST | `{ fromEmail, fromNickname }` | 새 친구 요청 |
| FRIEND_ONLINE | `{ email, online }` | 친구 온/오프라인 |
| NEW_MESSAGE | `{ chatId, senderNickname, preview }` | 새 메시지 (WS 미연결 시) |

---

## 에러 응답

포맷: `{ "code": "ERROR_CODE", "message": "설명" }`

| code | HTTP Status | 설명 |
|------|-------------|------|
| EMAIL_ALREADY_EXISTS | 409 | 이미 가입된 이메일 |
| INVALID_CREDENTIALS | 401 | 이메일/비밀번호 불일치 |
| USER_NOT_FOUND | 404 | 사용자 없음 |
| ALREADY_FRIENDS | 409 | 이미 친구 상태 |
| TOKEN_EXPIRED | 401 | 토큰 만료 |

---

## 비즈니스 규칙

- 자기 자신에게 친구 요청 불가
- 이미 친구인 상태에서 재요청 불가
- PENDING 상태인 요청이 존재하면 중복 요청 불가
- 친구 요청 수락 시 Friendship 레코드 생성, 거절 시 상태만 변경
- 채팅은 친구 간 1:1만 지원
- WebSocket 메시지 전송 시 상대방 세션이 없으면 DB에만 저장 후 SSE NEW_MESSAGE 알림
- 메시지 히스토리는 `before` 타임스탬프 기반 커서 페이지네이션 (limit 기본 50)

---

## 공통 규칙

- JSON 필드: camelCase
- 시간: ISO 8601 UTC (`2026-03-18T12:00:00Z`)
- 인증: JWT Bearer Token (모든 Protected API)
- 사용자 식별자: 이메일
- CORS 허용: `http://localhost:3000`

---

## WebSocket 핸들러 동작

```
연결 수립
  └─ 쿼리 파라미터에서 JWT 추출 → 사용자 인증
  └─ 인증 실패 시 연결 거부

메시지 수신 (클라이언트 → 서버)
  └─ JSON 파싱 → type 분기
      ├─ CHAT  → DB 저장 → 상대방에게 전달 (Sinks.Many)
      ├─ READ  → 해당 채팅방 메시지 읽음 처리
      └─ TYPING → 상대방에게 전달 (DB 저장 없음)

연결 종료
  └─ 사용자 세션 맵에서 제거
  └─ 온라인 상태 변경 브로드캐스트
```

세션 관리: `ConcurrentHashMap<String, WebSocketSession>` (이메일 → 세션 매핑)

---

## 현재 개발 상태

**Phase 0 (프로젝트 세팅) — 완료**
- Gradle 의존성 (WebFlux, R2DBC, H2, Lombok)
- application.yml (H2 인메모리, 디버그 로깅)
- 패키지 디렉토리 구조 생성

**Phase 1 (인증 시스템) — 진행 중**
- [x] User 엔티티 (`model/entity/User.java`)
- [x] UserRepository (`repository/UserRepository.java`)
- [ ] JwtProvider (`security/JwtProvider.java` — 빈 파일)
- [ ] JwtAuthenticationFilter (`security/JwtAuthenticationFilter.java` — 빈 파일)
- [ ] SecurityConfig (`config/SecurityConfig.java` — 빈 파일)
- [ ] AuthService
- [ ] AuthController
- [ ] GlobalExceptionHandler, ErrorCode

**아직 미구현**: Phase 2~6 전체 (친구, 채팅, SSE, CORS, 테스트, 배포)

---

## 아직 추가되지 않은 의존성

build.gradle에 아직 없지만 PLAN.md에서 필요한 의존성:
- `spring-boot-starter-security` (Spring Security)
- `spring-boot-starter-validation` (요청 유효성 검사)
- `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` (JWT)

---

## 개발 순서

```
Phase 0  프로젝트 세팅 ✅
  │
Phase 1  인증 (User, JWT, Security, 회원가입/로그인) ← 현재
  │
Phase 2  친구 (검색, 요청, 수락/거절, 목록)
  │
Phase 3  채팅 (REST + WebSocket, 1:1 메시지)
  │
Phase 4  SSE 알림 (친구 요청, 온라인 상태, 새 메시지)
  │
Phase 5  CORS, 에러 핸들링, 테스트
  │
Phase 6  배포 (Docker, Nginx, PostgreSQL)
```

---

## 참조 문서

| 문서 | 내용 |
|------|------|
| [PLAN.md](./PLAN.md) | 백엔드 개발 계획서 (Phase별 상세) |
| [API-CONTRACT.md](./API-CONTRACT.md) | API 계약 상세 스펙 (요청/응답 예시) |
| [ENDPOINTS.md](./ENDPOINTS.md) | 프론트엔드용 엔드포인트 요약 |
| [FRONT.md](./FRONT.md) | 프론트엔드 개발 스펙 |
