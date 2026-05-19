# Pet House Monitoring - Backend

## 프로젝트 개요
반려동물 스마트 하우스 모니터링 시스템의 백엔드 서버.
IoT 기기(펫하우스)와 MQTT로 통신하며, 센서 데이터 수집, 카메라 스트리밍, 기기 제어를 담당한다.

## 기술 스택
- Java 21, Spring Boot 3.5.12
- MySQL 8.0 (JPA + QueryDSL)
- InfluxDB (센서 시계열 데이터)
- EMQX (MQTT 브로커)
- Spring Security + JWT (ID/PW 기반 인증, OAuth2 없음)
- WebSocket (실시간 센서 푸시)
- WebRTC / HLS (카메라 스트리밍)

## 패키지 구조
```
com.capstone.pethouse
├── domain
│   ├── auth/          # JWT, SecurityConfig, 로그인
│   ├── User/          # 회원 CRUD (MemberController, MemberService)
│   ├── device/        # PetHouse CRUD, 프로비저닝
│   ├── sensor/        # 센서 데이터 수집/조회
│   ├── camera/        # WebRTC, HLS, PTZ, 녹화
│   ├── fan/           # 팬 스케줄 (구현 완료)
│   ├── supply/        # 급식/급수 스케줄 (구현 완료)
│   └── User/          # (User 패키지는 위 참조)
├── global
│   ├── config/        # SecurityConfig, MqttConfig, WebSocketConfig 등
│   ├── common/        # AuditingFields, BaseResponse 등
│   └── error/         # 예외 처리
└── infra
    └── mqtt/          # MqttPublisher, MqttSubscriber, 토픽 관리
```

## 코드 컨벤션
- 엔티티: `of()` 정적 팩토리 메서드 사용, `@NoArgsConstructor(access = PROTECTED)`
- DTO: Request/Response 분리, record 사용 권장
- API 경로: `/api` prefix는 `server.servlet.context-path`로 이미 설정됨
- 응답: 일관된 응답 구조 사용
- 테스트: 서비스 레이어 단위 테스트 필수

## 기존 구현 (이미 완성됨 - 건드리지 말 것)
- `fan/` - 팬 스케줄 CRUD (FanController, FanService, FanSchedule, FanScheduleDetail, FanLog)
- `supply/` - 급식/급수 스케줄 CRUD (SupplyController, SupplyService, SupplySchedule, SupplyLog)
- `auth/` - 로그인(웹/앱), JWT 발급/검증, SecurityConfig
- `User/` - 회원 CRUD 14개 API (가입, 목록, 상세, 수정, 삭제, 아이디찾기, 비밀번호 초기화 등)

## API 명세 (반드시 참조)
- 모든 API 구현은 **PET_API명세.xlsx** (`/Users/kimtaewoo/Downloads/PET_API명세.xlsx`)를 기준으로 한다.
- CLAUDE.md의 TASK 정의는 참고용이며, 명세와 충돌 시 명세를 따른다.

---

# 개발자 A 담당 영역 (5개 도메인, 총 44개)

## [TASK-1] MQTT 인프라 (5개) — 모든 IoT 통신의 기반 ✅ 구현 완료
우선순위: ★★★★★ (가장 먼저)
브랜치: `feat/31-mqtt-infra` (main에 머지됨)

### 구현 목표
EMQX 브로커와 Spring Boot 연동. 서버가 기기에 명령을 보내고(Publish), 기기로부터 데이터를 받는(Subscribe) 기반 인프라.

### 구현 항목
1. **EMQX Docker 설정** — docker-compose.yml에 EMQX 서비스 추가
2. **MqttConfig** — Spring Integration MQTT 설정 (InboundAdapter, OutboundAdapter)
3. **MqttPublisher** — 서버→기기 명령 발행 서비스
4. **MqttSubscriber** — 기기→서버 데이터 수신 리스너
5. **ACK 처리** — 명령 전달 확인 메커니즘 (QoS 1+ 응답 토픽)

### 토픽 체계
```
pet/{houseId}/sensor/data      — 센서 데이터 수신 (subscribe)
pet/{houseId}/sensor/alert     — 알림 발행 (publish)
pet/{houseId}/cam/stream       — 카메라 스트림 제어
pet/{houseId}/cam/ptz          — PTZ 제어 명령
pet/{houseId}/device/status    — 기기 상태 (online/offline)
pet/{houseId}/device/command   — 기기 제어 명령 (팬, 급식 등)
pet/{houseId}/device/ack       — 명령 ACK 응답
```

### 필요한 의존성
```gradle
implementation 'org.springframework.boot:spring-boot-starter-integration'
implementation 'org.springframework.integration:spring-integration-mqtt'
```

---

## [TASK-2] Auth — ID/PW 기반 인증 + 회원 CRUD (14개) ✅ 구현 완료
우선순위: ★★★★★ (MQTT와 동시 또는 직후)
브랜치: `feat/32-auth`

### 구현 목표
ID/PW 기반 인증 (웹: 세션, 앱: JWT) + 회원 CRUD API. PET_API명세 '회원' 시트 기준.

### 구현 항목 (PET_API명세 기준)
1. **POST /member/login** — 웹 로그인 (세션)
2. **POST /member/login-app** — 앱 로그인 (JWT 토큰 반환)
3. **GET /member/list** — 회원 목록 (페이징)
4. **POST /member/register** — 회원가입 (USER 권한 고정)
5. **POST /member/form** — 관리자 회원 등록 (권한 설정 가능)
6. **PUT /member/form** — 관리자 회원 수정
7. **GET /member/form/{seq}** — 단일 회원 조회 (seq 기준)
8. **GET /member/id/{memberId}** — 단일 회원 조회 (memberId 기준)
9. **GET /member/checkId** — 아이디 중복확인
10. **PUT /member** — 회원정보 수정
11. **DELETE /member** — 회원 삭제
12. **POST /member/find-id** — 아이디 찾기
13. **POST /member/verify-user** — 회원정보 검증
14. **POST /member/reset-password** — 비밀번호 초기화

### 추가된 의존성
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

### Member 엔티티 필드 (테이블: member)
- seq (PK), memberId, memberPw, memberName, memberPhone, roleCode (USER/ADMIN), enabled

---

## [TASK-3a] Device CRUD (5개)
우선순위: ★★★★☆

### 구현 목표
펫하우스 기기의 기본 CRUD API.

### 구현 항목
1. **기기 등록 (POST /devices)** — 신규 PetHouse 생성
2. **기기 목록 조회 (GET /devices)** — 사용자의 기기 리스트
3. **기기 상세 조회 (GET /devices/{houseId})** — 단일 기기 정보
4. **기기 수정 (PUT /devices/{houseId})** — 닉네임 등 정보 수정
5. **기기 삭제 (DELETE /devices/{houseId})** — 기기 제거 + 연관 데이터 정리

### PetHouse 엔티티 확장 필드
- serialNumber (시리얼 번호)
- macAddress (MAC 주소)
- firmwareVersion
- deviceType (HOUSE, COLLAR)
- provisioningStatus (PENDING, CLAIMED, ACTIVE)

---

## [TASK-3b] Provisioning (6개)
우선순위: ★★★☆☆ (TASK-3a 완료 후)

### 구현 목표
QR→BLE→Wi-Fi→Claim 프로비저닝 플로우 + MQTT 연결 상태 관리.

### 구현 항목
1. **QR 코드 생성 (POST /devices/qr/generate)** — 기기 시리얼 번호 → QR 코드 생성
2. **QR 코드 검증 (POST /devices/qr/verify)** — 앱에서 QR 스캔 → 시리얼 번호 반환
3. **프로비저닝 시작 (POST /devices/{houseId}/provision)** — BLE 연결 후 Wi-Fi 정보 전달 API
4. **기기 Claim (POST /devices/claim)** — 기기가 서버에 최초 연결 시 소유권 등록
5. **연결 상태 관리** — MQTT Last Will을 활용한 online/offline 추적
6. **시리얼 번호 검증 (GET /devices/serial/check)** — 시리얼 존재/사용 여부 확인

---

## [TASK-4] Sensor — CO2/온도/습도 (5개)
우선순위: ★★★☆☆

### 구현 목표
MQTT로 센서 데이터를 수신하여 InfluxDB에 저장하고, WebSocket으로 클라이언트에 실시간 푸시.

### 구현 항목
1. **MQTT Sensor Subscriber** — `pet/{houseId}/sensor/data` 토픽 구독, JSON 파싱
2. **InfluxDB 저장** — 시계열 데이터 저장 (온도, 습도, CO2)
3. **WebSocket 실시간 푸시** — STOMP를 통해 클라이언트에 실시간 데이터 전달
4. **알림 임계값 설정/조회** — 사용자별 온도/습도/CO2 임계값 CRUD
5. **센서 이력 조회 API** — 기간별 데이터 조회 (차트용, InfluxDB 쿼리)

### 필요한 의존성
```gradle
implementation 'org.springframework.boot:spring-boot-starter-websocket'
implementation 'com.influxdb:influxdb-client-java:7.2.0'
```

### InfluxDB Docker 설정
```yaml
# docker-compose.yml에 추가
influxdb:
  image: influxdb:2.7
  ports:
    - "8086:8086"
  environment:
    DOCKER_INFLUXDB_INIT_MODE: setup
    DOCKER_INFLUXDB_INIT_USERNAME: admin
    DOCKER_INFLUXDB_INIT_PASSWORD: ${INFLUXDB_PASSWORD}
    DOCKER_INFLUXDB_INIT_ORG: pethouse
    DOCKER_INFLUXDB_INIT_BUCKET: sensor_data
```

### 센서 데이터 JSON 포맷 (MQTT 수신)
```json
{
  "houseId": 1,
  "temperature": 25.3,
  "humidity": 60.2,
  "co2": 412.5,
  "timestamp": "2025-10-13T15:30:00"
}
```

---

## [TASK-5a] Camera 스트리밍 — WebRTC + HLS (7개)
우선순위: ★★☆☆☆ (마지막)

### 구현 목표
WebRTC signaling 서버 + HLS 스트리밍 기능.

### 구현 항목
**WebRTC Signaling (4개)**
1. POST `/cameras/{houseId}/offer` — WebRTC offer 전달
2. POST `/cameras/{houseId}/answer` — WebRTC answer 전달
3. POST `/cameras/{houseId}/ice-candidate` — ICE candidate 교환
4. DELETE `/cameras/{houseId}/session` — 스트리밍 세션 종료

**HLS 스트리밍 (3개)**
5. POST `/cameras/{houseId}/hls/start` — HLS 스트리밍 시작
6. DELETE `/cameras/{houseId}/hls/stop` — HLS 스트리밍 중지
7. GET `/cameras/{houseId}/hls/playlist` — .m3u8 플레이리스트 반환

---

## [TASK-5b] Camera 제어 — PTZ + 녹화 + 스냅샷 (11개)
우선순위: ★★☆☆☆ (TASK-5a 완료 후)

### 구현 목표
카메라 PTZ 제어 (MQTT 명령), 녹화 파일 관리, 스냅샷 캡처.

### 구현 항목
**PTZ 제어 (3개)**
1. POST `/cameras/{houseId}/ptz/move` — 카메라 이동 (pan/tilt)
2. POST `/cameras/{houseId}/ptz/zoom` — 줌 제어
3. POST `/cameras/{houseId}/ptz/preset` — 프리셋 위치 이동

**녹화 (5개)**
4. POST `/cameras/{houseId}/recording/start` — 녹화 시작
5. POST `/cameras/{houseId}/recording/stop` — 녹화 중지
6. GET `/cameras/{houseId}/recordings` — 녹화 파일 목록
7. GET `/cameras/{houseId}/recordings/{id}` — 녹화 파일 다운로드
8. DELETE `/cameras/{houseId}/recordings/{id}` — 녹화 파일 삭제

**스냅샷 (3개)**
9. POST `/cameras/{houseId}/snapshot` — 스냅샷 캡처
10. GET `/cameras/{houseId}/snapshots` — 스냅샷 목록
11. GET `/cameras/{houseId}/snapshots/{id}` — 스냅샷 다운로드

---

# 작업 스타일

- TASK를 받으면 해당 도메인의 모든 항목을 끝까지 구현한다. 중간에 멈추지 않는다.
- Entity, Repository, Service, Controller, DTO, Config, 테스트까지 전부 작성한다.
- 코드를 생략하거나 "나머지는 비슷하게..." 같은 축약을 하지 않는다.
- 빌드가 되는 상태로 완성한다.
- 구현이 끝나면 변경된 파일 목록과 다음 TASK 안내를 출력한다.

---

# 작업 요청 방법

새 대화에서 아래처럼 요청:
```
TASK-1 (MQTT 인프라) 구현해줘
```

## TASK 목록 및 의존 순서

```
TASK-1 (MQTT) → TASK-2 (Auth) → TASK-3a (Device CRUD) → TASK-3b (Provisioning) → TASK-4 (Sensor) → TASK-5a (Camera 스트리밍) → TASK-5b (Camera 녹화/PTZ)
```

| TASK | 내용 | 개수 | 상태 |
|------|------|------|------|
| TASK-1 | MQTT 인프라 (EMQX, Publisher/Subscriber, ACK) | 5개 | ✅ 완료 |
| TASK-2 | Auth (ID/PW 로그인 + JWT + 회원 CRUD) | 14개 | ✅ 완료 |
| TASK-3a | Device CRUD (등록/조회/수정/삭제/유형코드) | 5개 | 미구현 |
| TASK-3b | Provisioning (QR/BLE/Wi-Fi Claim/연결상태/시리얼검증) | 6개 | 미구현 |
| TASK-4 | Sensor (MQTT Subscribe→InfluxDB→WebSocket 푸시, 알림 임계값, 이력 조회) | 5개 | 미구현 |
| TASK-5a | Camera 스트리밍 (WebRTC signaling 4개 + HLS 3개) | 7개 | 미구현 |
| TASK-5b | Camera 제어 (PTZ 3개 + 녹화 5개 + 스냅샷 3개) | 11개 | 미구현 |
