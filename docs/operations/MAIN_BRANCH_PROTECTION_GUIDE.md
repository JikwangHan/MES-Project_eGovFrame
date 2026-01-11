# main 브랜치 보호 설정 안내문 (MES-Project_eGovFrame, 초보자용)

이 문서는 GitHub 저장소 `MES-Project_eGovFrame`의 **main 브랜치 보호 규칙**을
이미지와 동일한 화면 기준으로 설정하는 방법을 단계별로 설명합니다.

---

## 1) 기본 확인(이미지와 일치 여부)
아래 항목이 이미지와 같다면 정상입니다.
- Ruleset Name: `메인-브랜치-보호`
- Enforcement status: **Active**
- Bypass list: 비어 있음
- Target branches: `main`
- Require a pull request before merging: 체크됨
- Required approvals: `1`
- Require conversation resolution before merging: 체크됨
- Require status checks to pass: 체크됨
- Required checks: `smoke`, `build`, `mvn-verify`
- Block force pushes: 체크됨
- Restrict deletions: 체크됨
- Require linear history: 체크됨

---

## 2) 화면별 상세 설정 방법(이미지 기준)

### A. Ruleset Name
1. `Ruleset Name` 입력칸에 **메인-브랜치-보호**를 입력합니다.
2. 대안(영문)을 쓰려면 `main-branch-protection`을 입력합니다.

### B. Enforcement status
1. `Enforcement status` 드롭다운 클릭
2. **Active** 선택 (Disabled는 실제로 보호가 적용되지 않음)

### C. Bypass list
1. **Add bypass**를 누르지 않고 비워 둡니다.
2. 긴급 상황에서만 관리자 1명을 추가합니다.

### D. Target branches
1. **Add target** 클릭
2. **Include by pattern** 선택
3. 패턴 입력칸에 `main` 입력
4. `main`이 목록에 보이면 완료

### E. Rules > Branch rules
아래 항목을 체크합니다(이미지와 동일).
- Restrict deletions
- Require linear history
- Require a pull request before merging
- Require conversation resolution before merging
- Require status checks to pass
- Block force pushes

#### Required approvals
1. `Require a pull request before merging` 아래 `Required approvals`를 **1**로 설정

### F. Status checks (이미지 핵심)
1. `Require status checks to pass`가 체크되어 있어야 합니다.
2. `Status checks that are required`에 아래 3개가 있어야 합니다.
   - `smoke`
   - `build`
   - `mvn-verify`
3. 없으면 **Add checks** 클릭 → 검색창에 이름 입력 → 선택

> 체크 이름이 목록에 안 뜨면  
> CI(예: GitHub Actions)가 최소 1회 실행된 뒤에 다시 추가해야 합니다.

---

## 3) 설정 후 마지막 점검(체크리스트)
- Enforcement status: Active
- Target branches: main
- Required approvals: 1
- Status checks: smoke/build/mvn-verify 3개 모두 존재
- Block force pushes: 체크됨
- Restrict deletions: 체크됨

---

## 4) 추가 보완 권장(선택)
필요 시 아래 항목도 고려할 수 있습니다.
- Require branches to be up to date before merging: 켬 (최신 코드 기준 테스트 강제)
- Require signed commits: 서명 커밋 사용 시만

