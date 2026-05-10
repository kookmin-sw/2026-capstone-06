# Current Status - 2026-05-10

## 작업 요약: Swagger(SpringDoc) 도입 및 JWT 보안 완비

### 1. 수행된 작업
- **Swagger(SpringDoc) 라이브러리 추가:** `build.gradle`에 `springdoc-openapi-starter-webmvc-ui` 의존성 추가.
- **보안 설정 업데이트:** `SecurityConfig.java`에서 Swagger UI 및 API Docs 관련 경로를 `permitAll()`로 설정.
- **JWT 보안 기능 보완:** 
  - `User` 엔티티에 `refreshToken` 추가.
  - 웹/앱 로그인 모두 `accessToken`, `refreshToken`을 발급하도록 `AuthService` 및 `AuthController` 수정.
  - `/member/refresh` 엔드포인트 신설 및 토큰 갱신 로직 추가.

### 2. 다음 단계
- 실제 API 컨트롤러들에 Swagger 어노테이션(@Operation, @Parameter 등) 추가하여 문서 고도화.
- 프론트엔드 연동을 통한 로그인 및 토큰 갱신 프로세스 테스트.

