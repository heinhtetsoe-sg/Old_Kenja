#!/bin/sh
# ---------------------------------------
# $Id: withus_vqs.sh 56574 2017-10-22 11:21:06Z maeshiro $
# ウィザス、VQS連携
# ---------------------------------------

# 初期設定
DB2_DB=//localhost:50000/navidb
PG_DB=//10.252.12.221/m_navi
PG_USER=pgsql
PG_PW=

# クラスパス設定
CLASSPATH=.     # log4j.properties の為
CLASSPATH=$CLASSPATH:/waslib/nao_package.jar
CLASSPATH=$CLASSPATH:./kenja_batch.jar
CLASSPATH=$CLASSPATH:/waslib/commons-collections-3.0.jar
CLASSPATH=$CLASSPATH:/waslib/commons-lang-2.0.jar
CLASSPATH=$CLASSPATH:/waslib/commons-logging.jar
CLASSPATH=$CLASSPATH:/waslib/commons-dbutils-1.0.jar
CLASSPATH=$CLASSPATH:/opt/IBM/db2/V8.1/java/db2jcc.jar
CLASSPATH=$CLASSPATH:/opt/IBM/db2/V8.1/java/db2jcc_license_cu.jar
CLASSPATH=$CLASSPATH:/waslib/log4j-1.2.8.jar
CLASSPATH=$CLASSPATH:./pg74.216.jdbc2.jar	# PostgreSQL JDBCドライバ
CLASSPATH=$CLASSPATH:/usr/local/development/src/common/java/kenja_common.jar
CLASSPATH=$CLASSPATH:./withus_vqsServer.jar

# 日付を得る
DATE=`date '+%Y-%m-%d'`

# VQSと連携する
JAVA=/opt/WebSphere/AppServer/java/bin/java
$JAVA -cp $CLASSPATH  jp.co.alp.kenja.batch.withus.vqsServer.Main $DB2_DB  $PG_DB  $PG_USER  "$PG_PW"  $DATE
