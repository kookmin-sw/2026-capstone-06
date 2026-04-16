# AGENTS.md

## Project Overview: 
- 반려동물 스마트 하우스 모니터링 시스템의 백엔드 서버. IoT 기기(펫하우스)와 MQTT로 통신하며, 센서 데이터 수집, 카메라 스트리밍, 기기 제어를 담당한다.

## Environment: 
- Java 21, Spring Boot 3.5.12
- MySQL 8.0 (JPA + QueryDSL)
- InfluxDB (센서 시계열 데이터)
- EMQX (MQTT 브로커)
- Spring Security + JWT (ID/PW 기반 인증, OAuth2 없음)
- WebSocket (실시간 센서 푸시)
- WebRTC / HLS (카메라 스트리밍)

## Project Structure:
com.capstone.pethouse
├── domain
│   ├── auth/          # JWT, SecurityConfig, 로그인
│   ├── code/          # 병원 진료 과목 코드
│   ├── device/        # PetHouse CRUD, 프로비저닝
│   ├── enums/         # enum 타입들
│   ├── User/          # 회원 CRUD (MemberController, MemberService)
│   ├── sensor/        # 센서 데이터 수집/조회
│   ├── camera/        # WebRTC, HLS, PTZ, 녹화
│   ├── fan/           # 팬 스케줄 (구현 완료)
│   ├── supply/        # 급식/급수 스케줄 (구현 완료)
│   └── User/          # (User 패키지는 위 참조)
├── global
│   ├── common/        # AuditingFields, BaseResponse 등
│   ├── config/        # SecurityConfig, MqttConfig, WebSocketConfig 등
│   └── error/         # 예외 처리
└── infra
└── mqtt/          # MqttPublisher, MqttSubscriber, 토픽 관리

## Code Conventions: 
- 엔티티: of() 정적 팩토리 메서드 사용, @NoArgsConstructor(access = PROTECTED)
- DTO: Request/Response 분리, record 사용 권장
- API 경로: /api prefix는 server.servlet.context-path로 이미 설정됨
- 응답: 일관된 응답 구조 사용
- 테스트: 서비스 레이어 단위 테스트 필수

## 기존 구현 (이미 완성됨 - 건드리지 말 것)
- fan/ - 팬 스케줄 CRUD (FanController, FanService, FanSchedule, FanScheduleDetail, FanLog)
- supply/ - 급식/급수 스케줄 CRUD (SupplyController, SupplyService, SupplySchedule, SupplyLog)
- auth/ - 로그인(웹/앱), JWT 발급/검증, SecurityConfig
- User/ - 회원 CRUD 14개 API (가입, 목록, 상세, 수정, 삭제, 아이디찾기, 비밀번호 초기화 등)

## API 명세 (반드시 참조)
모든 API 구현은 PET_API명세.xlsx (/Pet-House-Monitoring/document/api.xlsx)를 기준으로 한다.

## 작업 프로세스
가장 먼저 todo/ 폴더 내의 최신 날짜 파일을 읽어 본인이 수행해야 할 태스크를 결정한다.
작업 완료 시 todo/ 파일의 체크박스를 [x]로 업데이트 한다.

## Context Management
모든 작업 세션이 끝나거나 중요한 변경 사항이 있을 때, context/current_status.md를 업데이트하여 현재 진행 상황을 요약한다.
작업 중 발견한 특이사항이나 기록해둘 만한 기술적 노트는 context/ 폴더에 자유롭게 기록한다.

## ADR (Architecture Decision Records)
프로젝트의 주요 기술적 결정 사항은 docs/adr/ 폴더에 마크다운 파일로 기록한다.
파일 이름은 "adr-001-20260416.md"와 같이 날짜를 포함한다.
새로운 ADR을 작성하거나 기존 ADR을 수정할 때는 다음 템플릿을 사용한다.

# adr-001-20260416: [제목]

## Status
- [ ] Proposed
- [x] Accepted
- [ ] Deprecated

## Context
[이 결정을 내리게 된 배경과 상황을 설명]

## Decision
[내린 결정을 명확하게 기술]

## Consequences
[이 결정으로 인해 발생하는 장점과 단점, 트레이드오프를 기술]


## Constraints (추가)
- 모든 코드 변경은 기존 fan/, supply/, auth/ 등 기존 작성된 패키지의 로직을 파괴하지 않는 범위 내에서 이루어져야 한다.
- todo/에 정의되지 않은 작업을 임의로 수행하지 않는다.
- 중요: CLAUDE.md 파일은 동료의 설정 파일이므로 절대 수정하거나 삭제하지 않는다.