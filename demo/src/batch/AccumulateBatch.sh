#!/bin/sh
# kanji=漢字
#############################################################################
# 累積データ一括生成処理。
# 使用例: 以下の様に cron に登録して使ったりする。
#     0 23 * * * AccumulateBatch.sh lion kenjadb Cron 1
# 注意: このファイルは EUC/LFのみ でなければならない。
# 作成日: 2007/01/16
# 作成者: takaesu
# $Id: AccumulateBatch.sh,v 1.3 2007/04/03 02:22:32 takaesu Exp $
#############################################################################

# ログファイルの文字コード対応
# TODO: もし可能なら、UTF-8でログファイルを作りたい。
export LANG=ja_JP.eucJP

# 設定
MAINCLASS=jp.co.alp.kenja.batch.AccumulateSummaryBatch
JAVA_HOME=/opt/WebSphere/AppServer/java
KENJA_HOME=/usr/local/development/src
CLASSPATH=$KENJA_HOME/batch/kenja_batch.jar

# 定数
CLASSPATH=$CLASSPATH:$KENJA_HOME/batch/kenja_common.jar
CLASSPATH=$CLASSPATH:$KENJA_HOME/batch;	# プロパティファイル等のリソース置き場
EXTLIB=$KENJA_HOME/common/java/extlib
CLASSPATH=$CLASSPATH:$EXTLIB/commons-beanutils.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-collections-3.0.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-dbcp-1.1.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-dbutils-1.0.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-lang-2.1.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-logging.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-pool-1.1.jar
CLASSPATH=$CLASSPATH:$EXTLIB/jakarta-oro-2.0.8.jar
CLASSPATH=$CLASSPATH:$EXTLIB/log4j-1.2.8.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/../lib/j2ee.jar
CLASSPATH=$CLASSPATH:/home/db2inst1/sqllib/java/db2java.zip

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
