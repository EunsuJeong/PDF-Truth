# PDF Truth (True’s) MVP 개발 이력 요약

## 프로젝트명
PDF Truth (True’s)

## 프로젝트 목표
- 광고/트래킹 없는 개인용 PDF 리더기 MVP 구현
- Android 8.0+ (API 26+) 지원, Jetpack Compose 기반

## Git/GitHub 정책
- master 직접 push 금지(예외 상황 외)
- 모든 기능/문서 작업은 feature/*, docs/* 브랜치에서 진행
- 커밋 후 원격 저장소에 push
- Pull Request(PR) 생성 및 리뷰 후 master 병합

## 지금까지 구현된 주요 기능
1. 프로젝트 구조 설계 및 PDF 첫 페이지 렌더링
2. 멀티페이지 LazyColumn 렌더링
3. 페이지 이동 및 현재 페이지 UI
4. Pinch Zoom, PDF 스케일링
5. LRU Bitmap 캐시, 열람 이력 저장
6. 북마크/최근 문서 UI
7. Double Tap Zoom, PDF 검색 구조
8. 썸네일 리스트, 렌더링 최적화 기반

## 주요 커밋 이력
- feat: 프로젝트 구조 및 PDF 첫 페이지 렌더링 MVP 구현
- feat: implement multipage PDF LazyColumn rendering
- feat: implement page navigation and current page tracking
- feat: implement pinch zoom and PDF scaling
- feat: implement bitmap cache and reading history persistence
- feat: implement bookmarks metadata and recent documents UI
- feat: implement double tap zoom and PDF search foundation
- feat: implement page thumbnails and rendering optimization foundation

## 앞으로의 정책
- 모든 개발 작업은 PR 기반으로 관리
- master 직접 push 금지
- 예외 상황 외에는 반드시 PR을 통해 병합
