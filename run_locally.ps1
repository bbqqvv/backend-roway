$env:JAVA_HOME = "D:\ProgramStudy\IntelliJ IDEA 2025.3.2\jbr"
Get-Content .env -ErrorAction SilentlyContinue | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
    $parts = $_ -split '=', 2
    if ($parts.Count -eq 2) {
        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        Set-Item -Path "Env:$name" -Value $value
    }
}
.\mvnw spring-boot:run -DskipTests
