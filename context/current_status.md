# Current Status - 2026-05-10

## 작업 요약: Swagger(SpringDoc) 도입 및 JWT 보안 완비

### 1. 수행된 작업
- **Swagger(SpringDoc) 라이브러리 추가:** `build.gradle`에 `springdoc-openapi-starter-webmvc-ui` 의존성 추가.
- **보안 설정 업데이트:** `SecurityConfig.java`에서 Swagger UI 및 API Docs 관련 경로를 `permitAll()`로 설정.
- **JWT 보안 기능 보완:** 
  - `User` 엔티티에 `refreshToken` 추가.
  - 웹/앱 로그인 모두 `accessToken`, `refreshToken`을 발급하도록 `AuthService` 및 `AuthController` 수정.
  - `/member/refresh` 엔드포인트 신설 및 토큰 갱신 로직 추가.
- **Swagger JWT 인증 설정:** Swagger UI에서 토큰을 입력할 수 있도록 `SwaggerConfig`를 통해 Authorize 기능을 활성화함.
- **Git 추적 제외 관리:** `.gitignore` 대상임에도 추적되고 있던 `read_code_api.py` 등의 파일들을 `git rm --cached`로 정리함.
- **Git 브랜치 통합:** `develop` 브랜치를 `main`으로 리베이스 통합하고 원격 `develop` 브랜치를 삭제하여 히스토리를 선형화함.
- **부하 테스트 환경 구축:** `docker-compose.yml`에 nGrinder Controller 및 Agent 설정을 추가함.

### 2. 다음 단계
- 실제 API 컨트롤러들에 Swagger 어노테이션(@Operation, @Parameter 등) 추가하여 문서 고도화.
- 프론트엔드 연동을 통한 로그인 및 토큰 갱신 프로세스 테스트.

