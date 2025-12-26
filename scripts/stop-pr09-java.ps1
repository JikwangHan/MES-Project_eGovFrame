$ErrorActionPreference = "SilentlyContinue"

$targets = Get-CimInstance Win32_Process | Where-Object {
    $_.Name -eq "java.exe" -and ($_.CommandLine -like "*ai-middleware-egov*" -or $_.CommandLine -like "*maven*")
}

foreach ($p in $targets) {
    try {
        Stop-Process -Id $p.ProcessId -Force
    } catch {
        # 종료 실패는 무시한다.
    }
}
