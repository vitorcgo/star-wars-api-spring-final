@echo off
setlocal EnableExtensions

set "MAVEN_VERSION=3.9.12"
set "MAVEN_CMD="
set "MAVEN_ARGS="

for /f "delims=" %%i in ('where /r "%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin" mvn.cmd 2^>nul') do (
  set "MAVEN_CMD=%%i"
  set "MAVEN_ARGS=-o"
  goto :run
)

for /f "delims=" %%i in ('where mvn.cmd 2^>nul') do (
  set "MAVEN_CMD=%%i"
  goto :run
)

echo Maven nao encontrado no cache local nem no PATH.>&2
exit /b 1

:run
call "%MAVEN_CMD%" %MAVEN_ARGS% %*
exit /b %errorlevel%
