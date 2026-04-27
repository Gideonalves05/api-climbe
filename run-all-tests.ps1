$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.10'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location 'c:\gestao-climbe-investimentos\api-climbe'
& .\mvnw.cmd test 2>&1 | Out-String | Write-Output
