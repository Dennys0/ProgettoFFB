# Script di test per verificare H2 Database e il layer di persistenza
$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME -and (Test-Path "C:\Program Files\Java\jdk-25.0.2")) {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
}

$netBeansMvn = "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd"
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn test-compile
} elseif (Test-Path $netBeansMvn) {
    & $netBeansMvn test-compile
}

& "$env:JAVA_HOME\bin\java.exe" -cp "target/test-classes;target/classes;$HOME\.m2\repository\com\h2database\h2\1.4.200\h2-1.4.200.jar" com.alchemist.hangover.database.DatabaseSelfTest
