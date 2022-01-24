@echo off
rem /**
rem  * kanji=äøéö
rem  * $Id: antall.bat 56573 2017-10-22 11:14:13Z maeshiro $
rem  * @author tamura
rem  */

set __ANT_OPTS=%ANT_OPTS%
set __ANT_ARGS=%ANT_ARGS%

set ANT_ARGS=%*

set DIRTOP=..
if exist .\kenja_common\build.xml (
    set DIRTOP=.
)

if NOT exist %DIRTOP%\kenja_common\build.xml (
    echo ERROR: not found dir 'kenja_common'
    goto :EOF
)


call :ant %DIRTOP%\kenja_common\build.xml
if ERRORLEVEL 1 exit /b 1

call :ant %DIRTOP%\kenja\build.xml
if ERRORLEVEL 1 exit /b 1

for /d %%i in (%DIRTOP%\kenja_hiro\*) do (
    if exist %%i\build.xml (
        if not exist %%i\skip-antall.txt (
            call :ant %%i\build.xml
            if ERRORLEVEL 1 exit /b 1
        )
    )
)

set ANT_OPTS=%__ANT_OPTS%
set ANT_ARGS=%__ANT_ARGS%

set __ANT_OPTS=
set __ANT_ARGS=

echo.
echo èIóπ
goto :EOF




:ant
    if %~z1 EQU 0 goto :EOF
    echo =============================== %1 == %ANT_OPTS% == %ANT_ARGS% ==
    call ant -buildfile %1
    if ERRORLEVEL 1 echo é∏îs %1
goto :EOF


