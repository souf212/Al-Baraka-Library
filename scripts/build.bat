@echo off
setlocal
cd /d "%~dp0.."
if exist ".tools\jdk-21.0.10+7\bin\java.exe" (
 set "JAVA_HOME=%CD%\.tools\jdk-21.0.10+7"
 set "PATH=%CD%\.tools\jdk-21.0.10+7\bin;%PATH%"
)
if exist ".tools\apache-maven-3.9.11\bin\mvn.cmd" (
 call ".tools\apache-maven-3.9.11\bin\mvn.cmd" -B "-Dmaven.repo.local=.tools/m2" clean verify
) else (
 call mvn -B clean verify
)
exit /b %errorlevel%
