@echo off
cd /d "%~dp0"
javac -cp "lib\jinput-2.0.9.jar" -d out scr\PS5ControllerVLC.java
if errorlevel 1 (
    echo.
    echo Compile failed.
    pause
    exit /b 1
)
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jinput-2.0.9.jar" PS5ControllerVLC
pause