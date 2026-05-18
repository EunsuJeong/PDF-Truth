# PDF Truth (True's)

## 프로젝트 목표
PDF Truth (True's)는 Android 기반의 전면 무료 오프라인 PDF 리더 MVP입니다.
핵심 목표는 기능 확장보다 안정성과 예측 가능한 동작을 우선하는 것입니다.

## 핵심 정책
- 전면 무료
- 광고 없음
- 로그인 없음
- 구독 없음
- 개인정보 수집 없음
- 외부 서버 연동 없음
- 오프라인 중심

## 현재 MVP 포함 기능
- PDF 파일 선택 (SAF)
- PDF 렌더링
- 여러 페이지 스크롤
- 페이지 이동
- Pinch Zoom
- Double Tap Zoom
- 썸네일 뷰
- 최근 문서
- 마지막 읽은 페이지 저장
- 북마크
- PDF 메타데이터 표시
- PDF 검색
- 검색 결과 이동 (이전/다음 포함)
- 오류 처리 (권한 만료, 파일 삭제/이동, 손상/미지원 PDF 등)

## MVP 제외 기능
- 메모
- 형광펜
- OCR
- 클라우드
- PDF 편집
- 계정/동기화

## 기술 스택
- Kotlin
- Android Jetpack Compose
- Android PdfRenderer
- PDFium 준비 의존성
- Room
- DataStore
- MVVM
- Kotlin Coroutines

## 빌드 방법
1. Android Studio에서 프로젝트를 엽니다.
2. Android SDK 환경을 준비합니다.
3. Gradle 동기화를 수행합니다.
4. Debug 빌드를 실행합니다.

참고: 현재 저장소 루트에 Gradle Wrapper 파일(gradlew, gradlew.bat)이 없을 수 있으므로,
IDE 기반 빌드 또는 로컬 Gradle 환경을 사용해 빌드합니다.

## 현재 상태
- MVP 안정화 단계에 근접
- PR #9에서 검색/오류 처리 안정화 작업 진행
- 다음 단계는 릴리즈 전 체크리스트 기반 검증 및 CI 상태 정리

## Git/PR Workflow
- 모든 작업은 PR 기반으로 진행
- master 직접 commit 금지
- master 직접 push 금지
- 작업 흐름
  1. master 최신화
  2. 작업 브랜치 생성
  3. 변경사항 commit/push
  4. PR 생성
  5. 리뷰 및 CI 통과 후 병합

## 릴리즈 문서
- 릴리즈 전 점검 항목: docs/release-readiness-checklist.md
- 알려진 제한사항: docs/known-limitations.md
