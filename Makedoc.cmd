@echo off
REM Generování Javadoc
javadoc -encoding UTF-8 -sourcepath .\src -cp .\lib\Jama-1.0.3.jar;.\lib\jfreechart-1.5.3.jar -d doc\javadoc -version -author src\*.java

