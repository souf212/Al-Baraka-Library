@echo off
setlocal
cd /d "%~dp0.."
if exist ".tools\jdk-21.0.10+7\bin\java.exe" set "PATH=%CD%\.tools\jdk-21.0.10+7\bin;%PATH%"
java -jar "target\baraka-library.jar" --backup
if errorlevel 1 (
 echo Backup failed. Review the error above.
 exit /b 1
)
