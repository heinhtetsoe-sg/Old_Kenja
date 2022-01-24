#!/bin/sh
# ---------------------------------------
# $Id: withus_csv_write.sh 56574 2017-10-22 11:21:06Z maeshiro $
# ウィザス、CSV書出し
# ---------------------------------------

# 初期設定
OUTDIR=/usr/local/development/DATA/CSV_DATA/CSV_DATA_SEND
DB=witestdb

# クラスパス設定
CLASSPATH=.	# log4j.properties の為
CLASSPATH=$CLASSPATH:./kenja_batch.jar
CLASSPATH=$CLASSPATH:/waslib/nao_package.jar
CLASSPATH=$CLASSPATH:/waslib/commons-lang-2.0.jar
CLASSPATH=$CLASSPATH:/waslib/commons-logging.jar
CLASSPATH=$CLASSPATH:/waslib/commons-dbutils-1.0.jar
CLASSPATH=$CLASSPATH:/opt/IBM/db2/V8.1/java/db2jcc.jar
CLASSPATH=$CLASSPATH:/opt/IBM/db2/V8.1/java/db2jcc_license_cu.jar
CLASSPATH=$CLASSPATH:/waslib/log4j-1.2.8.jar
CLASSPATH=$CLASSPATH:./withus_markserver_csv.jar

# 日付を得る
DATE=`date '+%Y-%m-%d'`

# DBからCSVを生成する
SCHOOLDIV=1	# 0=withus, 1=navi
java -cp $CLASSPATH  jp.co.alp.kenja.batch.withus.markServer.Main //localhost:50000/$DB  $DATE -out $OUTDIR $SCHOOLDIV

