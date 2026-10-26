@echo off
setlocal
cd /d "%~dp0.."
if exist ".tools\jdk-21.0.10+7\bin\java.exe" set "PATH=%CD%\.tools\jdk-21.0.10+7\bin;%PATH%"
if "%~1"=="" (
 echo Usage: scripts\restore.bat "backups\backup-name.db"
 exit /b 1
)
echo Stop the application first. This REPLACES the library database.
set /p "BARAKA_CONFIRM=Type RESTORE to explicitly confirm replacement: "
if not "%BARAKA_CONFIRM%"=="RESTORE" (
 echo Cancelled. No database was changed.
 exit /b 1
)
java -jar "target\baraka-library.jar" --restore "%~1" --confirm-replace
if errorlevel 1 (
 echo Restore failed. Review the error above.
 exit /b 1
)
