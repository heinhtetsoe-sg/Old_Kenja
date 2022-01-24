@echo off
rem $Id: kfu.bat 56601 2017-10-23 02:29:22Z maeshiro $

if ""%~s0"" == ""%~s1"" (
	echo.
	echo コマンドプロンプト（通称「DOS窓」）から使用して下さい
	echo.
	echo オプションなどは kfu -help を参照してください
	pause
	exit /b
)

if ""%1"" == """" (
	echo ERROR 引数が不足してます
	echo.
	echo オプションなどは kfu -help を参照してください
	echo.
	pause
	exit /b
)

setlocal

set _XCD=%CD%

set _BASE=%~dp0..\..
cd /d %_BASE%
set _BASE=%CD%

set _JAKARTA=%_BASE%\..\java_extlib\JAKARTA
if not exist %_JAKARTA%\nul (
    set _JAKARTA=%_BASE%\java_extlib\JAKARTA
)
cd /d %_JAKARTA%
set _JAKARTA=%CD%

cd /d %_XCD%

for %%i in ("%_JAKARTA%\commons-beanutils-1.6.1\*.jar") do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\commons-collections-3.0\*.jar") do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\commons-dbcp-1.1\*.jar")        do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\commons-dbutils-1.0\*.jar")     do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\commons-lang-2.0\*.jar")        do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\commons-logging-1.0.3\*.jar")   do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\commons-pool-1.1\*.jar")        do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\commons-io-1.2\*.jar")          do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\jakarta-log4j-1.2.8\*.jar")     do call "cpappend.bat" %%i
for %%i in ("%_JAKARTA%\jakarta-oro-2.0.8\*.jar")       do call "cpappend.bat" %%i

if not exist %~dp0..\e-classes\nul   goto L_JAR
if not exist %~dp0..\e-classes\jp\co\alp\tools\filedeploy\impl\CommandLineMain.class goto L_JAR

rem == for Eclipse ==
echo using e-classes
set CLASSPATH=%~dp0..\e-classes;%CLASSPATH%
goto L_EXEC

:L_JAR
echo using alp_alp_tools.jar
set CLASSPATH=%~dp0..\alp_alp_tools.jar;%CLASSPATH%
goto L_EXEC


:L_EXEC
java.exe -Denv.computername=%COMPUTERNAME% jp.co.alp.tools.filedeploy.impl.CommandLineMain %*

endlocal
