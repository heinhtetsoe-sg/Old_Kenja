@echo off
rem $Id: php_kenja.bat 56601 2017-10-23 02:29:22Z maeshiro $

rem Windowsのコマンドプロンプトで使用します。
rem php_kenja の ディレクトリに cd します。
rem 例: php_kenja tokio E KNJE330
rem 例: php_kenja tokio E

if ""%2"" == """" (
	echo ERROR 引数が不足してます
	echo.
	echo 例     : php_kenja tokio E KNJE330
	echo    又は: php_kenja tokio E
	echo.
	pause
	exit /b
)

if not ""%4"" == """" (
	echo ERROR 引数が多すぎます
	echo.
	echo 例     : php_kenja tokio E KNJE330
	echo    又は: php_kenja tokio E
	echo.
	pause
	exit /b
)

set _BASE=%~dp0\..\..\..\PHP\
set _R=%1
set _S=%2
set _ID=%3

if not exist %_BASE%\%_R%\development\nul (
    echo サーバー名が不正
    exit /b 1
)

if not exist %_BASE%\%_R%\development\src\%_S%\nul (
    echo サブシステムが不正
    exit /b 1
)

if ""%_ID%"" == """" goto L_CD

if not exist %_BASE%\%_R%\development\src\%_S%\%_ID%\nul (
    echo プログラムIDが不正
    exit /b 1
)

:L_CD
cd /d %_BASE%\%_R%\development\src\%_S%\%_ID%
