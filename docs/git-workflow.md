# PDF Truth (True’s) Git Workflow

## 브랜치 전략
- master: 배포/공개용 메인 브랜치
- feature/*: 기능 개발용 브랜치
- docs/*: 문서/정책 브랜치

## 개발/배포 절차

### 1. 작업 시작
```
git checkout master
git pull origin master
git checkout -b feature/<기능명>
```

### 2. 개발/커밋/푸시
```
git status
git diff
git add .
git commit -m "<type>: <message>"
git push -u origin feature/<기능명>
```

### 3. Pull Request(PR) 생성
```
gh pr create --base master --head feature/<기능명> --title "<PR 제목>" --body "<PR 내용>"
```

### 4. 리뷰/병합
- PR 리뷰 및 승인 후 master에 병합
- master 직접 push 금지(예외 상황 외)

## 참고
- 모든 작업은 PR 기반으로 관리
- 문서/정책 변경도 docs/* 브랜치에서 PR로 관리
