@echo off
echo Downloading Gradle...
echo Creating gradle wrapper directory if not exists...
if not exist "gradle\wrapper" mkdir gradle\wrapper

echo Downloading Gradle 8.6...
powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.6-bin.zip' -OutFile 'gradle-8.6-bin.zip'"

echo Extracting Gradle...
powershell -Command "Expand-Archive -Path 'gradle-8.6-bin.zip' -DestinationPath '.'"

echo Cleaning up...
del gradle-8.6-bin.zip

echo Setting up gradlew.bat...
echo @echo off > gradlew.bat
echo echo Starting Gradle... >> gradlew.bat
echo java -cp "gradle-8.6\lib\gradle-launcher-8.6.jar" org.gradle.launcher.GradleMain %%* >> gradlew.bat

echo Gradle setup complete!
echo.
echo To run the application, use: gradlew.bat bootRun
