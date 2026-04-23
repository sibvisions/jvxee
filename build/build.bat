@echo off

set JAVA_HOME=C:\Tools\Java\jdk1.5.0_15\

if ""%JAVA_HOME%""=="""" goto failure
goto javahomeok

:failure
echo JAVA_HOME is not set
goto end

:javahomeok
set cmdline=%1
if ""%1""=="""" goto start
shift

:setupArgs
if ""%1""=="""" goto start
set cmdline=%cmdline% %1
shift
goto setupArgs

:start
cd..
"%JAVA_HOME%\bin\java.exe" -classpath build/ant-launcher.jar org.apache.tools.ant.launch.Launcher -cp build/ant.jar;build/ant-trax.jar;build/xercesImpl.jar;build/xml-apis.jar

:end
pause