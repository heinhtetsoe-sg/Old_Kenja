#!/bin/sh
# kanji=漢字
#############################################################################
# 累積データ一括生成処理。
# 使用例: 以下の様に cron に登録して使ったりする。
#     0 23 * * * ReflectBatch.sh lion kenjadb batch
# 注意: このファイルは UTF-8/LFのみ でなければならない。
# 作成日: 2015/06/17
# 作成者: maesiro
# $Id: ReflectBatch.sh 56574 2017-10-22 11:21:06Z maeshiro $
#############################################################################

# ログファイルの文字コード対応
export LANG=ja_JP.UTF-8

# 設定
MAINCLASS=jp.co.alp.kenja.batch.HistoryReflectBatch
JAVA_HOME=/opt/IBM/WebSphere/AppServer/java
KENJA_HOME=/usr/local/development
CLASSPATH=$KENJA_HOME/batch/kenja_batch.jar

# 定数
CLASSPATH=$CLASSPATH:$KENJA_HOME/batch/kenja_common.jar
CLASSPATH=$CLASSPATH:$KENJA_HOME/batch;	# プロパティファイル等のリソース置き場
EXTLIB=$KENJA_HOME/src/common/java/extlib
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
if [ $# -lt 3 ]; then
	echo "Usage $0: <DB Name> <DB Host> <Staffcd> [yyyy-mm-dd]"
	echo "Ex1) $0 taka lion linux "
	echo "Ex2) $0 taka lion linux 2004-09-03"
	# TODO: 引数の説明
	exit 1;
else
	DBNAME=$1
	DBHOST=$2
	STAFFCD=$3
fi

if [ $# -eq 4 ]; then
	DATE=$4
else
	DATE=`date '+%Y-%m-%d'`
fi

# 準備
. /home/db2inst1/sqllib/db2profile

# 実行
$JAVA_HOME/bin/java $MAINCLASS dbname=$DBNAME dbhost=$DBHOST staffcd=$STAFFCD date=$DATE

# 終了
exit 0;
