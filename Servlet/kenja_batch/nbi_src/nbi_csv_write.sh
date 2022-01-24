#!/bin/sh
# ---------------------------------------
# $Id: nbi_csv_write.sh 56574 2017-10-22 11:21:06Z maeshiro $
# NBIグループウェア、CSV書出し
# ---------------------------------------

# check
if [ $# -lt 3 ]; then
	echo "Usage: $0 <DbName> <SchoolCode> <OutputDirectory> [yyyy-mm-dd]"
	exit 1
fi

# 初期設定
DB=$1
SCHOOL_CD=$2
OUTDIR=$3
if [ $4 ]; then
	DATE=$4
else
	DATE=`date '+%Y-%m-%d'`
fi

if [ ! -d $OUTDIR ]; then
	echo "Not Directory: $OUTDIR"
	exit 1
fi

LOG_PROPERTIES_FILE=./log4j.properties.${SCHOOL_CD}
if [ ! -f $LOG_PROPERTIES_FILE ]; then
	echo "Can not found log4j.properties file: $LOG_PROPERTIES_FILE"
	exit 1
fi

JAVA_HOME=/opt/WebSphere/AppServer/java

# クラスパス設定
CLASSPATH=.	# log4j.properties.?? の為
CLASSPATH=$CLASSPATH:./kenja_batch.jar
CLASSPATH=$CLASSPATH:/waslib/nao_package.jar
CLASSPATH=$CLASSPATH:/waslib/commons-collections-3.0.jar
CLASSPATH=$CLASSPATH:/waslib/commons-logging.jar
CLASSPATH=$CLASSPATH:/waslib/commons-dbutils-1.0.jar
CLASSPATH=$CLASSPATH:/opt/IBM/db2/V8.1/java/db2jcc.jar
CLASSPATH=$CLASSPATH:/opt/IBM/db2/V8.1/java/db2jcc_license_cu.jar
CLASSPATH=$CLASSPATH:/waslib/log4j-1.2.8.jar

# DBからCSVを生成する
$JAVA_HOME/bin/java -Dlog4j.configuration=$LOG_PROPERTIES_FILE \
	-cp $CLASSPATH \
	-jar nbi_groupware_csv.jar  //localhost:50000/$DB $DATE -out $OUTDIR $SCHOOL_CD
