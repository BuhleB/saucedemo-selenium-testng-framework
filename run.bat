@echo off
REM Runs a TestNG suite with Maven. Default: full regression on Chrome.
REM   run.bat
REM   run.bat src/test/resources/testng-smoke-only.xml
REM   run.bat src/test/resources/testng-sequential-browsers.xml
set SUITE=%1
if "%SUITE%"=="" set SUITE=src/test/resources/testng-full-regression.xml

call mvn clean test -DsuiteXmlFile=%SUITE%
set RESULT=%errorlevel%

echo.
echo Extent report folder: test-output\ExtentReports
exit /b %RESULT%
