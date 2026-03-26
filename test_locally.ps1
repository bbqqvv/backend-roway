$env:JAVA_HOME = "D:\ProgramStudy\IntelliJ IDEA 2025.3.2\jbr"
Get-Content .env -ErrorAction SilentlyContinue | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
    $parts = $_ -split '=', 2
    if ($parts.Count -eq 2) {
        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        Set-Item -Path "Env:$name" -Value $value
    }
}

if ($args.Count -eq 0) {
    Write-Host "Running all tests..." -ForegroundColor Green
    .\mvnw test
} else {
    $testName = $args[0]
    Write-Host "Running test for: $testName" -ForegroundColor Cyan
    .\mvnw test -Dtest=$testName
}
