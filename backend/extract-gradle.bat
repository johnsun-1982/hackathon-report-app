@echo off
echo Extracting Gradle 8.6...
echo Using built-in Windows tar extraction...
if exist "gradle-8.6-bin.zip" (
    echo Found gradle-8.6-bin.zip, extracting...
    
    REM Create gradle directory if not exists
    if not exist "gradle-8.6" mkdir "gradle-8.6"
    
    REM Use Windows built-in tar to extract (Windows 10+ has tar built-in)
    tar -xf gradle-8.6-bin.zip
    
    if exist "gradle-8.6" (
        echo Gradle 8.6 extracted successfully!
        echo Contents:
        dir gradle-8.6
    ) else (
        echo Failed to extract Gradle 8.6
    )
) else (
    echo gradle-8.6-bin.zip not found!
    echo Please download it first using download-gradle.bat
)

pause
