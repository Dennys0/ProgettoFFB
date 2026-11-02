@echo off
setlocal
cd /d "%~dp0"
echo.
echo THE ALCHEMIST'S HANGOVER - AVVIO
echo Richiede JDK 25 e Apache Maven.
echo.
where java >nul 2>nul
if errorlevel 1 (
  echo ERRORE: Java non trovato. Installa JDK 25 e imposta JAVA_HOME.
  pause
  exit /b 1
)
where mvn >nul 2>nul
if errorlevel 1 (
  echo ERRORE: Maven non trovato nel PATH.
  pause
  exit /b 1
)
echo Verifica la versione Java:
java -version
echo.
echo Compilazione e test...
call mvn -q clean package
if errorlevel 1 (
  echo ERRORE: compilazione o test falliti. Controlla i messaggi sopra.
  pause
  exit /b 1
)
echo.
echo Avvio del gioco...
call mvn -q exec:java
if errorlevel 1 (
  echo ERRORE durante l'avvio del gioco.
  pause
  exit /b 1
)
endlocal
