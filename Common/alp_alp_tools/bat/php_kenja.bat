@echo off
rem $Id: php_kenja.bat 56601 2017-10-23 02:29:22Z maeshiro $

rem Windows�̃R�}���h�v�����v�g�Ŏg�p���܂��B
rem php_kenja �� �f�B���N�g���� cd ���܂��B
rem ��: php_kenja tokio E KNJE330
rem ��: php_kenja tokio E

if ""%2"" == """" (
	echo ERROR �������s�����Ă܂�
	echo.
	echo ��     : php_kenja tokio E KNJE330
	echo    ����: php_kenja tokio E
	echo.
	pause
	exit /b
)

if not ""%4"" == """" (
	echo ERROR �������������܂�
	echo.
	echo ��     : php_kenja tokio E KNJE330
	echo    ����: php_kenja tokio E
	echo.
	pause
	exit /b
)

set _BASE=%~dp0\..\..\..\PHP\
set _R=%1
set _S=%2
set _ID=%3

if not exist %_BASE%\%_R%\development\nul (
    echo �T�[�o�[�����s��
    exit /b 1
)

if not exist %_BASE%\%_R%\development\src\%_S%\nul (
    echo �T�u�V�X�e�����s��
    exit /b 1
)

if ""%_ID%"" == """" goto L_CD

if not exist %_BASE%\%_R%\development\src\%_S%\%_ID%\nul (
    echo �v���O����ID���s��
    exit /b 1
)

:L_CD
cd /d %_BASE%\%_R%\development\src\%_S%\%_ID%
