@echo off

if "x%JAVA_HOME%" == "x" (@SET _JAVA=java)
if NOT "x%JAVA_HOME%" == "x" (@SET _JAVA=%JAVA_HOME%\bin\java)

call tpfenv.bat
"%_JAVA%" -Djavax.net.ssl.keyStore= -Djavax.net.ssl.keyStorePassword= -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dsun.lang.ClassLoader.allowArraySyntax=true -jar ..\pluginLib\tpf-boot.jar %*
if %ERRORLEVEL% NEQ 0 pause