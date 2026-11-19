@echo off
setlocal
set "MAVEN_PROJECTBASEDIR=%~dp0"
for %%i in ("%MAVEN_PROJECTBASEDIR%.") do set "MAVEN_PROJECTBASEDIR=%%~fi"
if defined JAVA_HOME (
	set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
	set "JAVACMD=java.exe"
)
"%JAVACMD%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
