# Backend 개발 계획서

> 관련 문서: [README.md](./README.md) · [API-CONTRACT.md](./API-CONTRACT.md) · [ENDPOINTS.md](./ENDPOINTS.md) · [FRONT.md](./FRONT.md)

---

## Phase 0 — 프로젝트 기반 세팅

### 0-1. Gradle 의존성 추가

현재 `build.gradle`에는 `spring-boot-starter`만 포함되어 있으므로 아래 의존성을 추가합니다.

```
spring-boot-starter-webflux          # WebFlux, Netty
spring-boot-starter-data-r2dbc       # Reactive DB 접근
spring-boot-starter-security         # Spring Security
spring-boot-starter-validation       # 요청 유효성 검사
io.jsonwebtoken:jjwt-api / impl / jackson  # JWT 생성/검증
io.r2dbc:r2dbc-h2 (개발) / r2dbc-postgresql (배포)  # DB 드라이버
```

### 0-2. 패키지 구조 생성

```
com.study.realtimechat/
├── config/          # WebSocketConfig, CorsConfig, SecurityConfig, R2dbcConfig
├── security/        # JwtProvider, JwtAuthenticationFilter
├── controller/      # AuthController, UserController, FriendController, ChatController, SseController
├── handler/         # ChatWebSocketHandler
├── service/         # AuthService, UserService, FriendService, ChatService, NotificationService
├── repository/      # UserRepository, FriendRequestRepository, ChatRepository, MessageRepository
├── model/
│   ├── entity/      # User, FriendRequest, Chat, Message
│   ├── dto/         # 각 API의 Request/Response DTO
│   └── enums/       # FriendRequestStatus, MessageType
└── exception/       # GlobalExceptionHandler, CustomException, ErrorCode
```

### 0-3. application.yml 설정

- R2DBC 데이터소스 (개발: H2, 배포: PostgreSQL)
- JWT secret key, 만료 시간 (Access: 30분, Refresh: 7일)
- CORS 허용 origin (`http://localhost:3000`)
- 서버 포트 (`8080`)

### 완료 기준

- [x] `./gradlew compileJava` 성공
- [x] 패키지 디렉토리 생성 완료
- [x] application.yml 설정 완료

---

## Phase 1 — 인증 시스템

### 1-1. 도메인 모델

**User 엔티티**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK, auto increment |
| email | String | 고유 식별자, unique |
| password | String | BCrypt 해시 |
| nickname | String | 표시 이름 |
| createdAt | Instant | 가입 시각 |

### 1-2. 구현 항목

| 순서 | 파일 | 설명 |
|:----:|------|------|
| 1 | `User.java` | 엔티티 정의 |
| 2 | `UserRepository.java` | `findByEmail` 리액티브 쿼리 |
| 3 | `JwtProvider.java` | Access/Refresh Token 생성, 검증, 파싱 |
| 4 | `SecurityConfig.java` | WebFlux Security 설정 — public/protected 경로 분리 |
| 5 | `JwtAuthenticationFilter.java` | 요청 헤더에서 토큰 추출 → 인증 객체 생성 |
| 6 | `AuthService.java` | 회원가입 (BCrypt 해시), 로그인 (토큰 발급), 토큰 갱신 |
| 7 | `AuthController.java` | `POST /api/auth/signup`, `login`, `refresh` |
| 8 | `GlobalExceptionHandler.java` | `ErrorCode` 기반 공통 에러 응답 |

### 1-3. 에러 코드

- `EMAIL_ALREADY_EXISTS` (409) — 회원가입 시 이메일 중복
- `INVALID_CREDENTIALS` (401) — 로그인 실패
- `TOKEN_EXPIRED` (401) — 토큰 만료

### 완료 기준

- [ ] 회원가입 → 로그인 → 토큰으로 인증 필요 API 호출 흐름 동작
- [ ] 만료된 토큰으로 요청 시 401 응답
- [ ] Refresh Token으로 Access Token 재발급 동작

---

## Phase 2 — 사용자 검색 & 친구 시스템

### 2-1. 도메인 모델

**FriendRequest 엔티티**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| fromEmail | String | 요청 보낸 사용자 |
| toEmail | String | 요청 받은 사용자 |
| status | Enum | `PENDING`, `ACCEPTED`, `REJECTED` |
| createdAt | Instant | 요청 시각 |

**Friendship 엔티티** (수락 시 생성)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| userEmail1 | String | 사용자 1 (정렬된 순서) |
| userEmail2 | String | 사용자 2 |
| createdAt | Instant | 친구 된 시각 |

### 2-2. 구현 항목

| 순서 | 파일 | 설명 |
|:----:|------|------|
| 1 | `FriendRequest.java`, `Friendship.java` | 엔티티 정의 |
| 2 | `FriendRequestRepository.java`, `FriendshipRepository.java` | 리액티브 레포지토리 |
| 3 | `UserService.java` | 이메일로 사용자 검색 |
| 4 | `UserController.java` | `GET /api/users/search?email={email}` |
| 5 | `FriendService.java` | 친구 요청, 수락/거절, 목록 조회 로직 |
| 6 | `FriendController.java` | 친구 관련 REST 엔드포인트 5개 |

### 2-3. 비즈니스 규칙

- 자기 자신에게 친구 요청 불가
- 이미 친구인 상태에서 재요청 불가 (`ALREADY_FRIENDS`)
- 이미 `PENDING` 상태인 요청이 존재하면 중복 요청 불가
- 수락 시 `Friendship` 레코드 생성, 거절 시 요청 상태만 변경

### 완료 기준

- [ ] 이메일 검색으로 사용자 조회
- [ ] 친구 요청 → 수락 → 친구 목록에 표시
- [ ] 중복 요청, 자기 자신 요청 등 예외 케이스 처리

---

## Phase 3 — 채팅 (REST + WebSocket)

### 3-1. 도메인 모델

**Chat 엔티티**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK (= chatId) |
| userEmail1 | String | 참여자 1 |
| userEmail2 | String | 참여자 2 |
| createdAt | Instant | 생성 시각 |

**Message 엔티티**

| 필드 | 타입 | 설명 |
|------|------|------|
| id | UUID | PK |
| chatId | UUID | FK → Chat |
| senderEmail | String | 보낸 사람 |
| content | String | 메시지 내용 |
| timestamp | Instant | 전송 시각 |
| read | Boolean | 읽음 여부 |

### 3-2. 구현 항목

| 순서 | 파일 | 설명 |
|:----:|------|------|
| 1 | `Chat.java`, `Message.java` | 엔티티 정의 |
| 2 | `ChatRepository.java`, `MessageRepository.java` | 리액티브 레포지토리 |
| 3 | `ChatService.java` | 채팅방 생성/조회, 메시지 저장, 히스토리 페이지네이션 |
| 4 | `ChatController.java` | `GET /api/chats`, `GET /api/chats/{chatId}/messages` |
| 5 | `ChatWebSocketHandler.java` | WebSocket 핸들러 — 핵심 구현 |
| 6 | `WebSocketConfig.java` | WebSocket 라우팅 (`/ws/chat`) |

### 3-3. WebSocket 핸들러 상세

```
연결 수립
  └─ 쿼리 파라미터에서 JWT 추출 → 사용자 인증
  └─ 인증 실패 시 연결 거부

메시지 수신 (클라이언트 → 서버)
  └─ JSON 파싱 → type 분기
      ├─ CHAT  → DB 저장 → 상대방에게 전달 (Sinks.Many)
      ├─ READ  → 읽음 처리 (아래 3-5 참조)
      └─ TYPING → 상대방에게 전달 (DB 저장 없음)

연결 종료
  └─ 사용자 세션 맵에서 제거
  └─ 온라인 상태 변경 브로드캐스트
```

### 3-5. READ (읽음 처리) 상세

클라이언트 → 서버: `{ "type": "READ", "chatId": "uuid" }`

**서버 처리 흐름:**

```
1. chatId로 Chat 조회 → 상대방 이메일 특정
   - Chat.userEmail1 / userEmail2 중 본인이 아닌 쪽이 상대방

2. MessageRepository에서 미읽음 메시지 일괄 업데이트
   - 조건: chatId 일치 AND senderEmail = 상대방 AND read = false
   - 처리: read = true로 UPDATE
   - 쿼리: UPDATE message SET read = true
          WHERE chat_id = :chatId
          AND sender_email = :senderEmail
          AND read = false

3. 상대방에게 READ 이벤트 전송
   - 세션 맵(ConcurrentHashMap)에서 상대방 세션 조회
   - 세션이 존재하면 → WebSocket으로 전송:
     { "type": "READ", "chatId": "uuid", "readerEmail": "본인 이메일" }
   - 세션이 없으면 → 전송하지 않음 (다음 접속 시 DB 상태로 확인)
```

**MessageRepository 필요 메서드:**

```java
@Modifying
@Query("UPDATE message SET read = true WHERE chat_id = :chatId AND sender_email = :senderEmail AND read = false")
Mono<Integer> markAsRead(UUID chatId, String senderEmail);
```

### 3-4. 세션 관리

- `ConcurrentHashMap<String, WebSocketSession>` — 이메일 → 세션 매핑
- 메시지 전송 시 상대방 세션이 존재하면 WebSocket으로 직접 전달
- 상대방 세션이 없으면 DB에만 저장 (SSE NEW_MESSAGE로 알림)

### 완료 기준

- [ ] 친구 목록에서 상대 클릭 → 채팅방 생성/진입
- [ ] WebSocket으로 실시간 메시지 송수신
- [ ] 메시지 히스토리 페이지네이션 동작
- [ ] 입력 중 표시, 읽음 처리 동작

---

## Phase 4 — SSE 알림

### 4-1. 구현 항목

| 순서 | 파일 | 설명 |
|:----:|------|------|
| 1 | `NotificationService.java` | `Sinks.Many` 기반 사용자별 이벤트 스트림 관리 |
| 2 | `SseController.java` | `GET /sse/notifications` — SSE 연결 엔드포인트 |
| 3 | 기존 서비스 연동 | FriendService, ChatService에서 이벤트 발행 |

### 4-2. 이벤트 발행 시점

| event | 발행 시점 | 발행 주체 |
|-------|----------|----------|
| `FRIEND_REQUEST` | 친구 요청 생성 시 | FriendService |
| `FRIEND_ONLINE` | WebSocket 연결/해제 시 | ChatWebSocketHandler |
| `NEW_MESSAGE` | 메시지 수신자의 WebSocket이 미연결일 때 | ChatService |

### 4-3. SSE 연결 관리

- `ConcurrentHashMap<String, Sinks.Many<ServerSentEvent>>` — 이메일 → SSE Sink 매핑
- 연결 시 JWT 인증 후 Sink 생성
- 연결 해제 시 Sink 제거
- heartbeat: 30초마다 빈 이벤트 전송 (연결 유지)

### 완료 기준

- [ ] SSE 연결 후 친구 요청 수신 시 실시간 알림
- [ ] 친구 온라인/오프라인 상태 변경 수신
- [ ] WebSocket 미연결 상태에서 새 메시지 알림 수신

---

## Phase 5 — CORS, 에러 핸들링, 테스트

### 5-1. CORS

- `CorsConfig.java` — `localhost:3000` 허용
- WebSocket handshake에도 CORS 적용

### 5-2. 글로벌 에러 핸들링

- `ErrorCode` enum — 모든 에러 코드 중앙 관리
- `@ControllerAdvice` + `@ExceptionHandler` — 공통 에러 응답 포맷 `{ code, message }`

### 5-3. 테스트

| 대상 | 테스트 종류 | 범위 |
|------|-----------|------|
| AuthService | 단위 테스트 | 회원가입 중복, 로그인 실패, 토큰 갱신 |
| FriendService | 단위 테스트 | 친구 요청 중복, 자기 자신 요청, 수락/거절 |
| ChatService | 단위 테스트 | 채팅방 생성, 메시지 저장, 페이지네이션 |
| JwtProvider | 단위 테스트 | 토큰 생성/검증/만료 |
| Auth API | 통합 테스트 | 회원가입 → 로그인 → 인증 플로우 |
| WebSocket | 통합 테스트 | 연결 → 메시지 송수신 → 연결 해제 |

### 완료 기준

- [ ] 프론트엔드(`localhost:3000`)에서 API 호출 시 CORS 에러 없음
- [ ] 모든 에러 응답이 `{ code, message }` 포맷
- [ ] 주요 서비스 단위 테스트 통과

---

## Phase 6 — 배포

### 6-1. DB 전환

- 개발: H2 (인메모리)
- 배포: PostgreSQL (R2DBC)
- `application-prod.yml` 프로파일 분리

### 6-2. 배포 구성

```
[Nginx]
  ├─ /              → Next.js (프론트엔드)
  ├─ /api/**        → Spring WebFlux (백엔드)
  ├─ /ws/**         → Spring WebFlux WebSocket (upgrade)
  └─ /sse/**        → Spring WebFlux SSE
```

### 6-3. 배포 옵션 (추후 결정)

- Docker Compose (Nginx + Spring Boot + PostgreSQL + Next.js)
- 클라우드: AWS EC2 / GCP / Railway 등

### 완료 기준

- [ ] Docker 이미지 빌드 및 실행
- [ ] 프로파일별 설정 동작 (dev / prod)
- [ ] Nginx 리버스 프록시를 통한 FE ↔ BE 통신

---

## 개발 순서 요약

```
Phase 0  프로젝트 세팅 (의존성, 패키지, 설정)
  │
Phase 1  인증 (User, JWT, Security, 회원가입/로그인)
  │
Phase 2  친구 (검색, 요청, 수락/거절, 목록)
  │
Phase 3  채팅 (REST + WebSocket, 1:1 메시지)     ← 핵심
  │
Phase 4  SSE 알림 (친구 요청, 온라인 상태, 새 메시지)
  │
Phase 5  CORS, 에러 핸들링, 테스트
  │
Phase 6  배포 (Docker, Nginx, PostgreSQL)
```

각 Phase는 이전 Phase가 완료된 후 진행합니다. Phase 완료 시마다 프론트엔드와 연동 테스트를 수행합니다.
