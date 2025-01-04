@echo off

:: Define source and output directories
set SRC_DIR=src
set OUT_DIR=out

:: Step 1: Compile Java classes
if not exist "%OUT_DIR%" (
    mkdir "%OUT_DIR%"
)

javac -d "%OUT_DIR%" "%SRC_DIR%\*.java"
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed. Exiting.
    exit /b 1
)

:: Step 2: Run the Node.Main class with arguments 1 through 6
for /L %%i in (1,1,6) do (
    start java -cp "%OUT_DIR%" Node.Main %%i
    echo Started process with argument %%i
)

echo All processes have been started.
pause
