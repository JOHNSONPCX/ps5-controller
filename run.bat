@echo off
cd /d "%~dp0"
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jinput-2.0.9.jar" PS5ControllerVLC
pause