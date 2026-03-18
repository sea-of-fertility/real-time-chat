# Real-Time Chat

WebSocket과 SSE(Server-Sent Events)를 활용한 1:1 실시간 채팅 애플리케이션입니다.
Spring WebFlux 기반의 리액티브 프로그래밍을 학습하기 위한 프로젝트입니다.

## 주요 기능

- 이메일 기반 회원가입 / 로그인 (JWT 인증)
- 이메일로 사용자 검색 및 친구 요청 / 수락 / 거절
- 친구 간 1:1 실시간 채팅 (WebSocket)
- 친구 요청 알림, 온라인 상태 알림 (SSE)

## 핵심 학습 포인트

- `WebSocketHandler` — WebSocket 연결 수립 및 메시지 핸들링
- `Sinks.Many` — 다수의 구독자에게 메시지를 발행하는 멀티캐스트 싱크
- `text/event-stream` Content-Type — SSE 엔드포인트 구성
- Reactor 기본 오퍼레이터 — `map`, `flatMap`, `filter`

## 기술 스택

- Java 25
- Spring Boot 4.0.3
- Spring WebFlux
- Lombok
- Gradle

## 프로젝트 구조

```
src/main/java/com/study/realtimechat/
├── RealTimeChatApplication.java
├── config/          # WebSocket, CORS, Security 설정
├── handler/         # WebSocketHandler 구현
├── controller/      # REST API, SSE 엔드포인트
├── model/           # User, Chat, Message 도메인 모델
├── repository/      # 데이터 접근 계층
├── service/         # 인증, 채팅, 친구 비즈니스 로직
└── security/        # JWT 토큰 처리
```

## 실행 방법

```bash
./gradlew bootRun
```

## 프론트엔드

별도 프로젝트(Next.js + TypeScript)로 개발하며, API 계약 사항은 [API-CONTRACT.md](./API-CONTRACT.md)를 참조합니다.

## 아키텍처 개요

```
┌─────────┐  WebSocket (1:1 채팅)  ┌──────────────┐  WebSocket  ┌─────────┐
│ User A  │◀══════════════════════▶│  Chat Server │◀═══════════▶│ User B  │
│(Next.js)│◀────── SSE ───────────│  (WebFlux)   │──── SSE ───▶│(Next.js)│
└─────────┘  (알림, 온라인 상태)    └──────────────┘             └─────────┘
                                         │
                                    ┌────┴────┐
                                    │   DB    │
                                    └─────────┘
```

- **WebSocket**: 친구 간 1:1 실시간 메시지 교환
- **SSE**: 친구 요청 알림, 온라인/오프라인 상태 푸시
- **REST API**: 인증, 사용자 검색, 친구 관리, 메시지 히스토리
