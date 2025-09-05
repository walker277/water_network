@echo off
REM Spuštění programu s knihovnami
java -cp .\bin;.\lib\Jama-1.0.3.jar;.\lib\jfreechart-1.5.3.jar WNVis_SP2024



@echo off
SETLOCAL

CALL Build.cmd
IF %ERRORLEVEL% NEQ 0 (
    echo Chyba: Kompilace selhala.
    EXIT /B 1
)

CALL Makedoc.cmd
IF %ERRORLEVEL% NEQ 0 (
    echo Chyba: Vytváření dokumentace selhalo.
)

java -cp ".\bin;.\lib\Jama-1.0.3.jar;.\lib\jfreechart-1.5.3.jar" WNVis_SP2024

ENDLOCAL

