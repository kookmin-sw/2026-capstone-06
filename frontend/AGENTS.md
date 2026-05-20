# AGENTS.md

## Project Overview: 
- 반려동물 스마트 하우스 모니터링 시스템의 프론트엔드 웹 애플리케이션. 사용자는 이 웹을 통해 반려동물의 상태를 실시간 모니터링하고, 기기(펫하우스)를 제어하며, 데이터를 조회할 수 있다.

## Environment: 
- React, TypeScript, Vite
- Zustand (전역 상태 관리)
- 기타: TailwindCSS / shadcn-ui 등 프로젝트 내 설정된 UI 라이브러리 사용
- 통신: REST API 호출 및 WebSocket/WebRTC (실시간 데이터 및 스트리밍 시)

## Project Structure:
src/
├── assets/          # 이미지, 폰트 등 정적 리소스
├── components/      # 재사용 가능한 UI 컴포넌트
├── hooks/           # 커스텀 React 훅
├── pages/           # 라우팅되는 각 페이지 (화면) 컴포넌트
├── services/        # API 호출 및 비즈니스 로직 연동
├── store/           # Zustand 스토어 정의
├── types/           # TypeScript 인터페이스/타입 정의
└── utils/           # 유틸리티 함수 및 공통 로직

## Code Conventions: 
- 컴포넌트: 함수형 컴포넌트(Functional Component) 및 화살표 함수 사용 권장. 파일 및 폴더명은 `PascalCase` 또는 `kebab-case` 등 프로젝트 기존 규칙을 준수한다.
- 훅(Hooks): 커스텀 훅은 `use` 접두사로 시작하며 `camelCase`로 작성.
- 상태 관리: 전역으로 필요한 상태만 Zustand store에 저장하며, UI 종속적인 지역 상태는 컴포넌트 내 `useState`를 활용한다.
- 타입 정의: 가능한 한 명시적인 타입(`interface` 또는 `type`)을 작성하고 `any` 사용을 지양한다.
- API 호출: 응답 데이터를 다루는 DTO 타입 모델을 사전에 정의하여 사용한다.

## API 명세 (반드시 참조)
모든 API 구현은 PET_API명세.xlsx (`document/api.xlsx`)를 기준으로 한다.

## 작업 프로세스
가장 먼저 `todo/` 폴더 내의 최신 날짜 파일을 읽어 본인이 수행해야 할 태스크를 결정한다.
작업 완료 시 `todo/` 파일의 체크박스를 `[x]`로 업데이트 한다.

## Context Management
모든 작업 세션이 끝나거나 중요한 변경 사항이 있을 때, `context/context-001-YYYYMMDD.md` 파일을 제작하여 현재 진행 상황을 요약한다.
작업 중 발견한 특이사항이나 기록해둘 만한 기술적 노트는 `context/` 폴더에 자유롭게 기록한다.
파일 이름은 "context-001-YYYYMMDD.md"와 같이 날짜를 포함한다.

## ADR (Architecture Decision Records)
프로젝트의 주요 기술적 결정 사항은 `docs/adr/` 폴더에 마크다운 파일로 기록한다.
파일 이름은 "adr-001-YYYYMMDD.md"와 같이 날짜를 포함한다.
새로운 ADR을 작성하거나 기존 ADR을 수정할 때는 다음 템플릿을 사용한다.

# adr-001-YYYYMMDD: [제목]

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
- `todo/`에 정의되지 않은 작업을 임의로 수행하지 않는다.
- 중요: 백엔드 환경이나 문서 포맷이 변경되었을 경우 즉시 동기화하여 작성한다.
- 중요: 타 AI 에이전트 환경설정 파일(예: `.windsurfrules`, `CLAUDE.md`, `AGENTS.md` 등)을 임의로 수정하거나 삭제하지 않는다.
