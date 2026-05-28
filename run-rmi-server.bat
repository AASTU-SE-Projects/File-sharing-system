@echo off
setlocal
cd /d "%~dp0"
REM Include lib jars (MySQL connector, JavaFX libs if needed) on the classpath
java -cp "bin;lib/*" rmi.RMIServer
