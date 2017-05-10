REM Set the 3 parameters before calling this shell script. 
REM SET "HUDSON_ROOT=%cd%"
REM SET "XXXJDK=C:\Hudson\SDK\XXX\jdk-6u45"
REM SET "ANT_HOME=C:\Hudson\SDK\XXX\ant-1.9.6"
REM %HUDSON_ROOT%\build_kit\buildWin\autobuild.bat
@echo off
echo "autobuild starting!"

set "ROOTDIR=%HUDSON_ROOT%"

echo %ROOTDIR%
echo %XXXJDK%
echo %ANT_HOME%
echo %JOB_NAME%
set "PATH=%XXXJDK%\bin;%ANT_HOME%\bin;%PATH%"

set "DIST_PATH=%ROOTDIR%\bin"

for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
if (%dt:~4,1%) == (0) set "MM=%dt:~5,1%"
set "yearstamp=%YY%"
set "monthstamp=%MM%"
set "daystamp=%DD%"

if (%monthstamp%) == (10) set monthstamp=A

if (%monthstamp%) == (11) set monthstamp=B

if (%monthstamp%) == (12) set monthstamp=C

set "VERSION_DATE=%yearstamp%%monthstamp%%daystamp%"

cd %ROOTDIR%\code\ContentPublishServer\src

for /f %%a in (.version) do (
	set "XXX_Version_Full=%%a"
    REM echo %%a 
)
echo %XXX_Version_Full%
set "XXX_Version=%XXX_Version_Full:~0,7%"
set "XXX_Major_Ver=%XXX_Version_Full:~0,1%"
set "XXX_Minor_Ver=%XXX_Version_Full:~6,1%"

echo %XXX_Version%

REM Check the tgz file existing or not, generate the build version
set loopcount=15
set loopidx=0

REM Loop begins
:loop

set /a loopcount=loopcount-1
set /a loopidx=loopidx+1
echo %loopcount%
echo %loopidx%

REM set the tar file name by loop index
set "TARNAME=%DIST_PATH%/tgz/ContentServer%VERSION_DATE%%loopidx%.tgz"
echo %TARNAME%

if %loopcount%==0 (
	goto exitloop
) else (
	if exist %TARNAME% (
		echo "The tar file exist!"
		rem file exists
		goto loop
	) else (
		rem file doesn't exist
		set "BUILDVERSION=%VERSION_DATE%%loopidx%"
		goto exitloop
	)
)	

:exitloop
echo --- make link path for tgz sharing folder ---
mkdir "%HUDSON_HOME%\XXX_Tgz"
mklink /j "%HUDSON_HOME%\XXX_Tgz\%JOB_NAME%" "%WORKSPACE%"

echo %BUILDVERSION%

echo "Ant starting!"
echo ROOTDIR=%ROOTDIR%
echo %ROOTDIR%\build_kit\buildWin

cd %ROOTDIR%\build_kit\buildWin

echo %ANT_HOME%\bin

call %ANT_HOME%\bin\ant -f %ROOTDIR%\build_kit\buildWin\buildWin.xml -Darg0=%VERSION_DATE% -Darg1=%BUILDVERSION% -Darg2=%XXX_Version% -Darg3=%XXX_Major_Ver% -Darg4=%XXX_Minor_Ver% -DrootDir=%ROOTDIR% -DXXXJdk=%XXXJDK% -DantHome=%ANT_HOME%

echo ----- Build End -----