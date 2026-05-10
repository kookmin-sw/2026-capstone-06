# Current Status - 2026-05-10

## 작업 요약: Swagger(SpringDoc) 도입 완료

### 1. 수행된 작업
- **Swagger(SpringDoc) 라이브러리 추가:** `build.gradle`에 `springdoc-openapi-starter-webmvc-ui` 의존성 추가.
- **보안 설정 업데이트:** `SecurityConfig.java`에서 Swagger UI 및 API Docs 관련 경로를 `permitAll()`로 설정하여 인증 없이 접근 가능하도록 수정.

### 2. 다음 단계
- 실제 API 컨트롤러들에 Swagger 어노테이션(@Operation, @Parameter 등) 추가하여 문서 고도화.
- Swagger UI를 통한 API 정상 작동 여부 교차 검증.

