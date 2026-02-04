#!/bin/bash

if [ "xJAVA_HOME" != "x" ];then
	_JAVA="${JAVA_HOME}/bin/java"
else
	_JAVA="java"
fi

. ./tpfenv.sh
${_JAVA} -Djavax.net.ssl.keyStore= -Djavax.net.ssl.keyStorePassword= -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword= -Dsun.lang.ClassLoader.allowArraySyntax=true -jar ../pluginLib/tpf-boot.jar $*
