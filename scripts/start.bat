@echo off
setlocal
cd /d "%~dp0.."
if exist ".tools\jdk-21.0.10+7\bin\java.exe" set "PATH=%CD%\.tools\jdk-21.0.10+7\bin;%PATH%"
where java >nul 2>nul
if errorlevel 1 (
 echo Java 21 is required. Install a JDK and add its bin directory to PATH.
 exit /b 1
)
if not exist "target\baraka-library.jar" (
 echo Missing target\baraka-library.jar. Run: mvn clean verify
 exit /b 1
)
if not exist data mkdir data
if not exist backups mkdir backups
echo Al Baraka Library: http://localhost:8080
echo Keep this window open. Press Ctrl+C to stop safely.
java -Xms64m -Xmx256m -jar "target\baraka-library.jar"
if errorlevel 1 (
 echo Application failed. Review the error above.
 pause
 exit /b 1
)
