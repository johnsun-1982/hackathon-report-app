@echo off
echo Starting Spring Boot application with Gradle...
echo Java version:
java -version
echo.
echo Gradle version:
.\gradlew.bat --version
echo.
echo Building and running application...
.\gradlew.bat bootRun
