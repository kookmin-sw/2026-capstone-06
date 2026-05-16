# Current Status - 2026-05-16

## 작업 요약: Sensor 데이터 저장 방식 변경 및 CI/CD 파이프라인 구축

### 1. 수행된 작업
- **Sensor 데이터 저장 로직 수정**:
  - `IotDataService.java`에서 IoT 데이터를 `HouseData`가 아닌 `Sensor` 엔티티에 직접 저장하도록 변경.
  - `Sensor` 엔티티 및 `SensorRepository` 신규 생성.
- **GitHub Actions CI/CD 구축**:
  - `.github/workflows/deploy.yml` 생성.
  - `main` 브랜치 push 시 자동으로 JAR 빌드 및 Docker 이미지를 Amazon ECR로 전송하는 워크플로우 구성.
- **빌드 검증**: `.\gradlew.bat compileJava`를 통해 코드의 정상 컴파일 여부 확인 완료.

### 2. 다음 단계
- GitHub Secrets 설정 안내 및 실제 push를 통한 CI/CD 동작 확인.
- AWS EC2 환경에서 ECR로부터 최신 이미지를 pull 하여 실행하는 자동 배포 단계(SSH) 고려.
- Sensor 데이터 저장 방식 고도화 (InfluxDB 연동 등).
