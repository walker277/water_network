@echo off
REM Vytvoří složku bin pokud neexistuje
if not exist bin mkdir bin

REM Poskládá classpath (ručně)
set CP=.\lib\Jama-1.0.3.jar;.\lib\jfreechart-1.5.3.jar;.\src

REM Kompilace
javac -encoding UTF-8 -cp %CP% -d .\bin src\*.java

