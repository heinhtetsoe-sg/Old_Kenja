@echo off
set VRQ_UTIL=vrq-util.jar
set VRQ_CORE=vrq-core.jar
set VRQ_CONN=vrq-connect.jar
set JDOM=jdom.jar
set JIDE_LIBS=jide-common.jar;jide-components.jar;jide-dialogs.jar;jide-grids.jar

javaw -classpath %VRQ_UTIL%;%VRQ_CORE%;%VRQ_CONN%;%JDOM%;%JIDE_LIBS% jp.co.fit.queryutil.Main