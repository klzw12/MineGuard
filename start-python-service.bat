@echo off
chcp 65001 >nul
echo Starting Python service...

:: Activate Conda environment
call conda activate ai-extend
if %errorlevel% neq 0 (
    echo Failed to activate Conda environment. Please ensure conda is installed and environment exists.
    pause
    exit /b 1
)

:: Start Python service
echo Environment activated successfully. Starting Python service...
python "C:\Users\31783\Desktop\MineGuard\python-service\main.py"

:: Check if startup was successful
if %errorlevel% neq 0 (
    echo Python service startup failed.
    pause
    exit /b 1
)

echo Python service started successfully.
pause
