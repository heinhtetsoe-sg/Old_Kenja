#!/bin/sh
# kanji=漢字
#############################################################################
# 累積データ一括生成処理。
# 使用例: 以下の様に cron に登録して使ったりする。
#     0 23 * * * AccumulateBatch.sh lion kenjadb Cron 1
# 注意: このファイルは EUC/LFのみ でなければならない。
# 作成日: 2007/01/16
# 作成者: takaesu
# $Id: AccumulateBatch.sh 63869 2018-12-10 15:45:58Z maeshiro $
#############################################################################

# ログファイルの文字コード対応
# TODO: もし可能なら、UTF-8でログファイルを作りたい。
export LANG=ja_JP.UTF-8

# 設定
MAINCLASS=jp.co.alp.kenja.batch.AccumulateSummaryBatch
JAVA_HOME=/opt/IBM/WebSphere/AppServer/java
KENJA_HOME=`pwd`/..
CLASSPATH=$KENJA_HOME/batch/kenja_batch.jar

# 定数
CLASSPATH=$CLASSPATH:$KENJA_HOME/batch/kenja_common.jar
CLASSPATH=$CLASSPATH:$KENJA_HOME/batch;	# プロパティファイル等のリソース置き場
EXTLIB=$KENJA_HOME/common/java/extlib
CLASSPATH=$CLASSPATH:$EXTLIB/commons-beanutils.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-collections-3.0.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-dbcp-1.1.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-dbutils-1.0.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-lang-2.0.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-logging.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-pool-1.1.jar
CLASSPATH=$CLASSPATH:$EXTLIB/jakarta-oro-2.0.8.jar
CLASSPATH=$CLASSPATH:$EXTLIB/log4j-1.2.8.jar
# 以下のクラスパスは Base1でのみ必要なのかも。
CLASSPATH=$CLASSPATH:/opt/WebSphere/AppServer/lib/j2ee.jar

# チェック
if [ $# -lt 4 ]; then
	echo "Usage $0: <DB Name> <DB Host> <Staffcd> <Baseday> [yyyy-mm-dd]"
	echo "Ex1) $0 taka lion linux 1"
	echo "Ex2) $0 taka lion linux 1 2004-09-03"
	# TODO: 引数の説明
	exit 1;
else
	DBNAME=$1
	DBHOST=$2
	STAFFCD=$3
	BASEDAY=$4
fi

if [ $# -eq 5 ]; then
	DATE=$5
else
	DATE=`date '+%Y-%m-%d'`
fi

# 準備
. /home/db2inst1/sqllib/db2profile

# 実行
$JAVA_HOME/bin/java $MAINCLASS dbname=$DBNAME dbhost=$DBHOST staffcd=$STAFFCD baseday=$BASEDAY date=$DATE

# 終了
exit 0;
