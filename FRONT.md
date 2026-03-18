# Frontend 기능 명세서

> 프로젝트: Real-Time Chat
> 프레임워크: Next.js (TypeScript)
> 백엔드: Spring WebFlux — API 스펙은 [API-CONTRACT.md](./API-CONTRACT.md), 엔드포인트 요약은 [ENDPOINTS.md](./ENDPOINTS.md) 참조

---

## 1. 페이지 구성

| 경로 | 페이지 | 인증 필요 | 설명 |
|------|--------|:---------:|------|
| `/login` | 로그인 | ✗ | 이메일/비밀번호 로그인 |
| `/signup` | 회원가입 | ✗ | 이메일/비밀번호/닉네임 입력 |
| `/chat` | 채팅 목록 | ✓ | 1:1 채팅방 리스트 (메인 화면) |
| `/chat/[chatId]` | 채팅방 | ✓ | 1:1 메시지 주고받기 |
| `/friends` | 친구 관리 | ✓ | 친구 목록, 검색, 요청 관리 |

---

## 2. 기능 상세

### 2-1. 회원가입 (`/signup`)

- 입력 필드: 이메일, 비밀번호, 비밀번호 확인, 닉네임
- 클라이언트 유효성 검사
  - 이메일 형식 검증
  - 비밀번호 최소 8자
  - 비밀번호 확인 일치 여부
  - 닉네임 필수 입력
- API: `POST /api/auth/signup`
- 성공 시 로그인 페이지로 이동
- 에러 처리: `EMAIL_ALREADY_EXISTS` → "이미 가입된 이메일입니다" 표시

### 2-2. 로그인 (`/login`)

- 입력 필드: 이메일, 비밀번호
- API: `POST /api/auth/login`
- 성공 시 JWT 토큰 저장 후 `/chat`으로 이동
- 토큰 저장 위치: httpOnly cookie 또는 메모리 (보안 고려)
- 에러 처리: `INVALID_CREDENTIALS` → "이메일 또는 비밀번호가 올바르지 않습니다" 표시

### 2-3. 토큰 관리

- Access Token 만료 시 자동으로 `POST /api/auth/refresh` 호출
- Refresh Token도 만료된 경우 `/login`으로 리다이렉트
- API 요청 시 `Authorization: Bearer {accessToken}` 헤더 자동 포함 (Axios interceptor 또는 fetch wrapper)

---

### 2-4. 채팅 목록 (`/chat`) — 메인 화면

- 진입 시 `GET /api/chats` 호출하여 채팅방 목록 표시
- 각 채팅방 항목에 표시할 정보
  - 상대방 닉네임
  - 마지막 메시지 미리보기
  - 마지막 메시지 시간 (상대 시간 표시: "방금", "3분 전", "어제")
  - 읽지 않은 메시지 수 (뱃지)
- 정렬: 마지막 메시지 시간 기준 최신순
- 채팅방 클릭 시 `/chat/[chatId]`로 이동
- SSE `NEW_MESSAGE` 이벤트 수신 시 목록 실시간 갱신

### 2-5. 채팅방 (`/chat/[chatId]`)

#### 메시지 표시
- 진입 시 `GET /api/chats/{chatId}/messages?limit=50` 호출
- 내 메시지는 오른쪽, 상대 메시지는 왼쪽 정렬
- 각 메시지에 전송 시간 표시
- 날짜가 바뀌면 날짜 구분선 삽입

#### 메시지 전송
- WebSocket(`ws://{host}/ws/chat?token={accessToken}`)으로 연결
- 입력창에서 메시지 작성 후 전송 → WebSocket `type: "CHAT"` 메시지 발행
- 전송 후 입력창 초기화, 스크롤 최하단 이동

#### 스크롤 페이지네이션
- 위로 스크롤 시 `GET /api/chats/{chatId}/messages?before={가장 오래된 메시지 timestamp}&limit=50` 호출
- 이전 메시지를 위에 추가하되 스크롤 위치 유지

#### 입력 중 표시
- 사용자가 타이핑 시 WebSocket `type: "TYPING"` 전송
- 상대방의 `TYPING` 수신 시 "상대방이 입력 중..." 표시 (일정 시간 후 자동 소멸)

#### 읽음 처리
- 채팅방 진입 시 WebSocket `type: "READ"` 전송하여 읽음 처리

---

### 2-6. 친구 관리 (`/friends`)

#### 친구 목록 탭
- `GET /api/friends` 호출하여 친구 목록 표시
- 각 친구 항목: 닉네임, 이메일, 온라인 상태 (초록 점)
- 친구 클릭 시 해당 친구와의 채팅방으로 이동
  - 채팅방이 없으면 자동 생성 (첫 메시지 전송 시)

#### 사용자 검색
- 이메일 입력으로 사용자 검색: `GET /api/users/search?email={email}`
- 검색 결과에서 "친구 요청" 버튼 표시
- 친구 요청: `POST /api/friends/request`
- 이미 친구인 경우 `ALREADY_FRIENDS` → "이미 친구입니다" 표시
- 자기 자신 검색 시 친구 요청 버튼 비노출

#### 받은 요청 탭
- `GET /api/friends/requests/received` 호출
- 각 요청 항목: 보낸 사람 닉네임, 이메일, 요청 시간
- "수락" / "거절" 버튼: `PUT /api/friends/request/{requestId}` 호출
- SSE `FRIEND_REQUEST` 이벤트 수신 시 실시간 갱신 및 알림 표시

---

## 3. 실시간 연결

### 3-1. WebSocket

- 로그인 후 WebSocket 연결 수립: `ws://{host}/ws/chat?token={accessToken}`
- 앱 전체에서 단일 연결 유지 (Context 또는 전역 상태로 관리)
- 연결 끊김 시 자동 재연결 (exponential backoff)
- 수신 메시지 타입별 처리

| type | 처리 |
|------|------|
| `CHAT` | 해당 채팅방에 메시지 추가, 채팅 목록 갱신 |
| `TYPING` | 해당 채팅방에 "입력 중" 표시 |
| `ONLINE` | 친구 목록에서 온라인 상태 업데이트 |

### 3-2. SSE

- 로그인 후 SSE 연결: `GET /sse/notifications` (Bearer 토큰 포함)
- 수신 이벤트별 처리

| event | 처리 |
|-------|------|
| `FRIEND_REQUEST` | 친구 요청 알림 표시 (토스트 또는 뱃지) |
| `FRIEND_ONLINE` | 친구 온라인/오프라인 상태 반영 |
| `NEW_MESSAGE` | 현재 열려있지 않은 채팅방의 새 메시지 알림 |

---

## 4. 인증 가드

- `/chat`, `/chat/[chatId]`, `/friends` 페이지는 로그인 필수
- 미인증 상태로 접근 시 `/login`으로 리다이렉트
- Next.js middleware 또는 layout 단위에서 토큰 유효성 검증

---

## 5. 에러 처리 공통

모든 API 에러 응답은 `{ code, message }` 형태이며, 프론트에서는 `code` 기반으로 분기 처리합니다.

| code | 사용자 메시지 | 처리 |
|------|-------------|------|
| `INVALID_CREDENTIALS` | 이메일 또는 비밀번호가 올바르지 않습니다 | 로그인 폼에 에러 표시 |
| `EMAIL_ALREADY_EXISTS` | 이미 가입된 이메일입니다 | 회원가입 폼에 에러 표시 |
| `USER_NOT_FOUND` | 사용자를 찾을 수 없습니다 | 검색 결과 영역에 표시 |
| `ALREADY_FRIENDS` | 이미 친구입니다 | 토스트 알림 |
| `TOKEN_EXPIRED` | (자동 처리) | Refresh Token으로 갱신 시도 → 실패 시 로그인 이동 |

---

## 6. 상태 관리

| 상태 | 범위 | 관리 방식 |
|------|------|----------|
| 인증 토큰 | 전역 | Context + httpOnly cookie |
| 현재 사용자 정보 | 전역 | Context (email, nickname) |
| WebSocket 인스턴스 | 전역 | Context (단일 연결) |
| SSE 인스턴스 | 전역 | Context (단일 연결) |
| 채팅 목록 | `/chat` | 페이지 상태 + 실시간 갱신 |
| 채팅 메시지 | `/chat/[chatId]` | 페이지 상태 + WebSocket 수신 |
| 친구 목록 | `/friends` | 페이지 상태 + SSE 갱신 |

---

## 7. 환경 변수

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080
```
