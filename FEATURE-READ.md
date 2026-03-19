# 읽음 처리 (READ) 기능 명세

> 대상: FE (Next.js), APP (Flutter) 개발자
> 관련: [API-CONTRACT.md](./API-CONTRACT.md) — WebSocket 섹션

---

## 개요

1:1 채팅에서 상대방이 메시지를 읽었는지 표시하는 기능.
카카오톡의 안 읽음 숫자 `1` 표시/제거 방식과 동일.

---

## WebSocket 프로토콜

### 클라이언트 → 서버 (READ 전송)

```json
{ "type": "READ", "chatId": "uuid" }
```

### 서버 → 클라이언트 (READ 수신)

```json
{ "type": "READ", "chatId": "uuid", "readerEmail": "friend@example.com" }
```

---

## 서버 동작

클라이언트가 READ를 전송하면 서버는 다음을 순서대로 처리:

1. **chatId로 Chat 조회** → 상대방 이메일 특정
2. **미읽음 메시지 일괄 업데이트** → 해당 chatId에서 상대방이 보낸 메시지 중 `read = false`인 것을 모두 `read = true`로 변경
3. **상대방에게 READ 이벤트 전송** → 상대방의 WebSocket 세션이 연결되어 있으면 `{ "type": "READ", "chatId": "...", "readerEmail": "..." }` 전송. 미연결 시 전송하지 않음 (다음 접속 시 DB 상태로 확인)

---

## 클라이언트 구현 가이드

### 1. READ 전송 타이밍

| 상황 | 동작 |
|------|------|
| 채팅방 진입 (화면 열림) | READ 전송 |
| 채팅방이 열린 상태에서 새 메시지 수신 | 즉시 READ 전송 |
| 탭/앱이 백그라운드에서 포그라운드로 복귀 | READ 전송 |
| 탭/앱이 비활성(백그라운드) 상태 | 전송하지 않음 |

**FE (Next.js)**: 브라우저 `focus` / `visibilitychange` 이벤트로 포그라운드 복귀 감지

**APP (Flutter)**: `WidgetsBindingObserver.didChangeAppLifecycleState`로 포그라운드 복귀 감지

### 2. READ 수신 처리

READ 이벤트를 수신하면 → 해당 `chatId`에서 **내가 보낸 메시지**들의 읽음 상태를 업데이트.

```
수신: { "type": "READ", "chatId": "abc", "readerEmail": "friend@example.com" }

→ chatId가 "abc"인 채팅방에서
→ 내가 보낸 메시지 중 안 읽음 표시(`1`)가 있는 것들을
→ 모두 읽음 처리 (표시 제거)
```

### 3. UI 표시

| 위치 | 표시 방법 |
|------|----------|
| 채팅 메시지 버블 | 내가 보낸 메시지 옆에 안 읽음 숫자 `1` 표시. READ 수신 시 `1` 제거 |
| 채팅방 목록 | `GET /api/chats` 응답의 `unreadCount`로 안 읽음 배지 표시. READ 전송 후 로컬에서 `unreadCount = 0` 처리 |

### 4. 초기 로딩 시 읽음 상태

- `GET /api/chats/{chatId}/messages` 응답에 각 메시지의 `read` 필드가 포함되지 않음 (현재 스펙)
- 채팅방 진입 시 READ를 전송하므로, 진입 이전의 안 읽음 상태는 `GET /api/chats`의 `unreadCount`로만 표시
- 내가 보낸 메시지의 읽음 여부는 **READ 이벤트 수신 시점부터** 실시간 반영

---

## 흐름 예시

```
A가 B에게 메시지 전송:
  A 화면: 메시지 옆에 "1" 표시 (B가 아직 안 읽음)

B가 채팅방 진입:
  B → 서버: { "type": "READ", "chatId": "..." }
  서버: B가 안 읽은 A의 메시지들 read = true로 업데이트
  서버 → A: { "type": "READ", "chatId": "...", "readerEmail": "B" }
  A 화면: 메시지 옆 "1" 제거
```

---

## 주의사항

- READ는 **채팅방 단위**로 동작 (개별 메시지 단위 아님)
- READ 전송 시 해당 채팅방의 상대방이 보낸 미읽음 메시지 **전체**가 읽음 처리됨
- 상대방이 오프라인이면 READ 이벤트가 전달되지 않지만, DB에는 반영됨
