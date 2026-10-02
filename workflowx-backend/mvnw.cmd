@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~n0)
@SET ___MVNW_UNFOLLOWED_ARGUMENT=
@SET __MVNW_CMD_LINE_ARGS=%*

@SETLOCAL
@SET MAVEN_PROJECTBASEDIR=%~dp0
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" (SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%)

@IF "%MVNW_VERBOSE%"=="true" (
  @ECHO MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%
)

@SET MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
@SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties
@SET MVNW_REPOURL=
@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=

@FOR /F "usebackq tokens=1,2 delims==" %%a IN ("%MAVEN_WRAPPER_PROPERTIES%") DO @(
  @IF "%%a"=="distributionUrl" (SET DISTRIBUTION_URL=%%b)
  @IF "%%a"=="wrapperUrl" (SET MVNW_REPOURL=%%b)
)

@SET DISTRIBUTION_FILENAME=%DISTRIBUTION_URL:\=/%
@FOR %%i IN (%DISTRIBUTION_FILENAME:/= %) DO (SET DISTRIBUTION_FILENAME=%%i)
@SET MAVEN_HOME=%MAVEN_USER_HOME%
@IF "%MAVEN_HOME%"=="" (SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%DISTRIBUTION_FILENAME:~0,-4%\%DISTRIBUTION_FILENAME:~0,-4%)

@SET MAVEN_EXECUTABLE=%MAVEN_HOME%\bin\mvn.cmd
@IF NOT EXIST "%MAVEN_EXECUTABLE%" (
  @ECHO Downloading Apache Maven to %MAVEN_HOME%
  @CALL :downloadMaven
  @IF ERRORLEVEL 1 (
    @ECHO ERROR: Failed to download Maven
    @EXIT /B 1
  )
)

@IF "%MVNW_VERBOSE%"=="true" (
  @ECHO Using Maven at %MAVEN_EXECUTABLE%
)

@CALL "%MAVEN_EXECUTABLE%" %__MVNW_CMD_LINE_ARGS%
@EXIT /B %ERRORLEVEL%

:downloadMaven
  @SET MAVEN_DIST_URL=%DISTRIBUTION_URL%
  @POWERSHELL -Command " ^
    $webclient = New-Object System.Net.WebClient; ^
    $webclient.Headers.Add('User-Agent', 'Maven Wrapper'); ^
    New-Item -ItemType Directory -Path '%MAVEN_HOME%' -Force | Out-Null; ^
    $zip = '%MAVEN_HOME%.zip'; ^
    Write-Host 'Downloading %MAVEN_DIST_URL%'; ^
    $webclient.DownloadFile('%MAVEN_DIST_URL%', $zip); ^
    Add-Type -AssemblyName System.IO.Compression.FileSystem; ^
    $parent = Split-Path '%MAVEN_HOME%' -Parent; ^
    [System.IO.Compression.ZipFile]::ExtractToDirectory($zip, $parent); ^
    Remove-Item $zip; ^
    Write-Host 'Done'"
@EXIT /B %ERRORLEVEL%
