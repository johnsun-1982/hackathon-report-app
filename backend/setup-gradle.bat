@echo off
echo ========================================
echo Setting up Gradle for Spring Boot
echo ========================================
echo.

echo Step 1: Downloading Gradle 8.6...
if not exist "gradle-8.6-bin.zip" (
    echo Downloading Gradle 8.6...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.6-bin.zip' -OutFile 'gradle-8.6-bin.zip'}"
    if exist "gradle-8.6-bin.zip" (
        echo Download completed successfully!
    ) else (
        echo Download failed!
        pause
        exit /b 1
    )
) else (
    echo Gradle 8.6 already downloaded.
)

echo.
echo Step 2: Extracting Gradle 8.6...
if not exist "gradle-8.6" (
    echo Extracting Gradle 8.6...
    powershell -Command "Expand-Archive -Path 'gradle-8.6-bin.zip' -DestinationPath '.' -Force"
    if exist "gradle-8.6" (
        echo Extraction completed successfully!
    ) else (
        echo Extraction failed!
        pause
        exit /b 1
    )
) else (
    echo Gradle 8.6 already extracted.
)

echo.
echo Step 3: Setting up gradlew.bat...
echo @echo off > gradlew.bat
echo echo Starting Gradle... >> gradlew.bat
echo java -cp "gradle-8.6\lib\gradle-launcher-8.6.jar" org.gradle.launcher.GradleMain %%* >> gradlew.bat

echo.
echo Step 4: Testing Gradle setup...
if exist "gradlew.bat" (
    echo gradlew.bat created successfully!
    echo Testing Gradle version...
    .\gradlew.bat --version
) else (
    echo Failed to create gradlew.bat!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Gradle setup completed!
echo You can now run: gradlew.bat bootRun
echo ========================================
pause
