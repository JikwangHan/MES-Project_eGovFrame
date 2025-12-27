# 서비스별 Chat 운영 구조(초안)

## 운영 구조 개요
- 공통 기준 Chat 1개 + 서비스별 Chat 3개
- 공통 기준은 반드시 공통 Chat에서만 확정

## 폴더 구조(권장)
```
docs/
  standards/
    COMMON_STANDARDS.md
  operations/
    CHAT_RULES.md
    PR_WORKFLOW.md
```

## 문서 운영 방식
- 공통 기준 문서: `docs/standards/COMMON_STANDARDS.md`
- 서비스별 진행 기록: 각 서비스별 별도 문서
- 일별 진행 파일: 로컬 전용(이미 규정된 위치 사용)

## PR 운영 방식(한 번에 완료)
- PR 하나 = 기능 + 스모크 + 문서
- 완료 기준에 PASS 라인 포함
- PR 산출물 목록은 `docs/PR-전체-산출물-정리.md`에 반영

## 서비스별 Chat 운영 규칙
- 공통 기준 변경 필요 시 공통 Chat에서 요청
- 서비스 Chat은 해당 서비스 코드/문서만 변경
- 연동 이슈는 공통 Chat에서 검토 후 조정
