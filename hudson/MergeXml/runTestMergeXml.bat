REM * one - D:\Hudson\workspace\wcmtgz\build_kit\buildWin\MergeXml\WEB-INF\web.xml
REM * two - D:\Hudson\workspace\wcmtgz\build_kit\buildWin\MergeXml\WEB-INF\generated_web.xml
SET "one=D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF\\web.xml"
SET "two=D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF\\generated_web.xml"
SET "three="D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF\\tmp\\web.xml"

SET "zero=D:\\Hudson\\workspace\\wcmtgz\\build_kit\\buildWin\\MergeXml\\WEB-INF"

REM echo "Run 1 parameters. path:%zero%"
REM java -jar MergeXml.jar %zero%

echo "Run 3 parameters."
java -jar MergeXml.jar %one% %two% %three%