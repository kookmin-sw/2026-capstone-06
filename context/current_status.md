# Current Status - 2026-05-05

## 작업 요약: PK/FK 숫자 기반 리팩토링 및 연관관계 개선 완료

### 1. 수행된 작업
- **Code 엔티티:** `String code` PK에서 `Long seq` (Identity) PK로 전환. `groupCode` 문자열 참조를 `@ManyToOne Code parent` 객체 참조로 변경하여 계층 구조를 객체 지향적으로 개선.
- **Device & User 연관관계:** `Device` 엔티티의 `String memberId`를 `@ManyToOne User user`로 변경. Repository 및 Service 로직에서 회원 조회 및 조인 방식을 객체 기반으로 최적화.
- **Hospital & Code 연관관계:** `Hospital` 엔티티의 `String mainMedCode`를 `@ManyToOne Code`로, `String` 리스트였던 `medCodes`를 `@ManyToMany List<Code>` (브릿지 테이블 방식)로 변경.
- **PetHouse & Code 연관관계:** `PetHouse` 엔티티의 `String objectCode`를 `@ManyToOne Code`로 변경.
- **전체 통합 검증:** `./gradlew clean build -x test`를 통해 컴파일 및 빌드 성공 확인.

### 2. 기술적 결정 (ADR 요약)
- **성능 최적화:** 문자열 조인 대신 BIGINT(Long) 조인을 사용하여 인덱스 효율성 및 조인 성능 향상.
- **데이터 무결성:** 객체 참조와 FK 제약 조건을 통해 데이터 참조 무결성 확보.
- **API 호환성:** 외부 API 응답(DTO)에서는 기존의 문자열 코드 형식을 유지하여 프론트엔드 영향을 최소화함.

### 3. 다음 단계
- 실제 DB 데이터 마이그레이션 스크립트 작성 (운영 환경 적용 시).
- 추가적인 성능 프로파일링.
