#!/bin/sh
# ---------------------------------------
# $Id: withus_icass_migration.sh 56574 2017-10-22 11:21:06Z maeshiro $
# ICASSデータ⇒ウィザスのデータ移行
# ---------------------------------------

# check
if [ $# -lt 2 ]; then
	echo "Usage: $0 <DbName> <FQCN...>"
	exit 1
fi

# 初期設定
DB=$1
shift
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

# 実行
$JAVA_HOME/bin/java -cp $CLASSPATH -jar withus_icass_migration.jar  //localhost:50000/$DB $@
