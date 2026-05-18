# Current Status - 2026-05-18

## 작업 요약: 불필요한 목걸이 센서 코드 및 HTTP IoT 패키지(iot 폴더) 전체 정리

### 1. 수행된 작업
- **목걸이 센서 코드 정리**:
  - `InfluxWriter.java`, `SensorPushService.java`에서 목걸이(neck_sensor) 관련 상수 및 메서드(`writeNeck`, `pushNeck`) 제거.
  - `SensorResponse.java` DTO에서 `heartVal`(심박수) 필드 제거 및 관련 생성자 호출부(`HouseDataService.java`, `ChartService.java`, `ChartServiceTest.java`) 일괄 수정.
- **HTTP IoT 패키지 완전 삭제**:
  - 100% MQTT 통신 구조로 통일함에 따라, 미사용되는 `com.capstone.pethouse.domain.iot` 메인 소스 폴더 및 테스트 폴더를 통째로 안전하게 삭제 처리 완료.
- **빌드 검증**: `.\gradlew.bat testClasses`를 실행하여 컴파일 및 빌드 정상 완료 확인 (`BUILD SUCCESSFUL in 26s`).

### 2. 다음 단계
- `todo-007-20260505.md`에 기재된 숫자 기반의 PK/FK 관계 마이그레이션 작업 진행 예정.
