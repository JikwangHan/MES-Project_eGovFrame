# PR-16: DB 설정 가이드(초안)

## 목적
- DB 접속 정보는 코드/문서에 저장하지 않고 환경 변수로 주입한다.

## 접속 방식(권장)
- 환경 변수 사용
  - `MES_DB_URL`
  - `MES_DB_USER`
  - `MES_DB_PASSWORD`

## 예시(로컬 MariaDB 기준)
- 개발 DB 이름(확정): `mes_dev`
- URL 형식 예시(실제 값은 로컬 환경에 맞게 입력)
  - `jdbc:mariadb://localhost:3306/mes_dev`

## 설정 방법(Windows PowerShell)
```powershell
$env:MES_DB_URL="jdbc:mariadb://localhost:3306/mes_dev"
$env:MES_DB_USER="db_user"
$env:MES_DB_PASSWORD="db_password"
```

## 설정 방법(Linux/macOS)
```bash
export MES_DB_URL="jdbc:mariadb://localhost:3306/mes_dev"
export MES_DB_USER="db_user"
export MES_DB_PASSWORD="db_password"
```

## 주의사항
- 비밀번호는 GitHub/문서/로그에 기록하지 않는다.
- `DbSupport`는 세 값이 모두 있을 때만 JDBC를 활성화한다.
