# EvoShot

실시간 멀티플레이어 2D 슈팅 게임 서버

## 개요

EvoShot은 Kotlin과 Netty를 기반으로 한 고성능 실시간 멀티플레이어 슈팅 게임 서버입니다. WebSocket을 통해 클라이언트와 통신하며, 60 FPS 게임 루프로 부드러운 게임 경험을 제공합니다.

## 기술 스택

- **Language**: Kotlin 2.1
- **JDK**: 21
- **Network**: Netty 4.1 (WebSocket)
- **Serialization**: kotlinx.serialization (JSON)
- **Concurrency**: Kotlin Coroutines
- **Build**: Gradle + Shadow Plugin
- **Test**: JUnit 5, AssertJ, MockK

## 프로젝트 구조

```
src/main/kotlin/com/evoshot/
├── core/
│   ├── controller/       # 게임 컨트롤러 및 메시지 처리
│   │   └── message/      # JSON 메시지 코덱
│   ├── domain/           # 도메인 모델 (Player, Bullet, Room)
│   ├── engine/           # 게임 엔진 (물리, 충돌 감지, 게임 루프)
│   └── util/             # 유틸리티 (벡터 연산)
├── network/
│   ├── handler/          # WebSocket 핸들러
│   ├── server/           # 게임 서버 및 정적 파일 서버
│   └── session/          # 세션 관리
└── EvoShotApplication.kt # 애플리케이션 진입점
```

## 핵심 기능

### 게임 엔진
- **60 FPS 게임 루프**: 일정한 틱 레이트로 게임 상태 업데이트
- **물리 시뮬레이션**: 중력, 점프, 이동 처리
- **충돌 감지**: Spatial Grid를 활용한 효율적인 충돌 검사
- **총알 시스템**: 방향 계산, 이동, 피격 판정

### 네트워크
- **WebSocket 통신**: 실시간 양방향 통신
- **세션 관리**: 플레이어 연결/해제 처리
- **메시지 브로드캐스트**: 룸 내 전체 플레이어에게 게임 상태 전송
- **Epoll 지원**: Linux 환경에서 고성능 I/O

### 게임 플레이
- **룸 기반 매칭**: 최대 2명 플레이어 대전
- **플레이어 조작**: 이동, 점프, 사격
- **HP 시스템**: 피격 시 HP 감소, 사망 처리

## 실행 방법

### 요구 사항
- JDK 21

### 빌드
```bash
./gradlew build
```

### 실행
```bash
./gradlew run
```

또는 Shadow JAR로 실행:
```bash
./gradlew shadowJar
java -jar build/libs/EvoShot-1.0-SNAPSHOT-all.jar
```

### 서버 포트
- **8080**: WebSocket 게임 서버 (`ws://localhost:8080/ws`)
- **80**: 정적 파일 서버 (테스트 클라이언트)

## WebSocket API

### 클라이언트 → 서버

**Connect** - 게임 연결
```json
{
  "type": "connect",
  "playerName": "Player1"
}
```

**PlayerInput** - 플레이어 입력
```json
{
  "type": "player_input",
  "dx": 1,
  "jump": false,
  "mouseX": 500.0,
  "mouseY": 300.0,
  "shoot": true
}
```

**Ping** - 지연시간 측정
```json
{
  "type": "ping",
  "timestamp": 1234567890
}
```

### 서버 → 클라이언트

**Connected** - 연결 성공
```json
{
  "type": "connected",
  "playerId": "uuid",
  "players": [...]
}
```

**GameState** - 게임 상태 (매 틱 전송)
```json
{
  "type": "game_state",
  "tick": 100,
  "players": [...],
  "bullet": [...]
}
```

**PlayerJoin** - 플레이어 입장
```json
{
  "type": "player_join",
  "player": {...}
}
```

**PlayerLeave** - 플레이어 퇴장
```json
{
  "type": "player_leave",
  "playerId": "uuid"
}
```

**PlayerDead** - 플레이어 사망
```json
{
  "type": "player_dead",
  "playerId": "uuid"
}
```

## 테스트

```bash
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21-tem && ./gradlew test
```

특정 테스트 클래스 실행:
```bash
source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21-tem && ./gradlew test --tests "GameEngineTest"
```

## 테스트 클라이언트

브라우저에서 `http://localhost/test-client.html` 접속하여 테스트 클라이언트를 사용할 수 있습니다.

## 라이선스

MIT License

