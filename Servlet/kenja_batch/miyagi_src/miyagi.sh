#!/bin/sh
# kanji=漢字
# ---------------------------------------
# $Id: miyagi.sh 56574 2017-10-22 11:21:06Z maeshiro $
# 宮城県　グループウェア連携
# 使用例:
#     ./miyagi.sh //172.31.120.21:50000/miyadb01 db2inst1 db2kenja //172.31.120.10/renkei renkei renkei //172.31.120.21:50000/iinkaidb db2inst1 db2kenja 2016-06-03
# ---------------------------------------

# 設定
MAINCLASS=jp.co.alp.kenja.batch.miyagi.Main
JAVA_HOME=/opt/IBM/WebSphere/AppServer/java
KENJA_HOME=/usr/local/development

# クラスパス設定
CLASSPATH=.     # log4j.properties の為
EXTLIB=$KENJA_HOME/src/common/java/extlib
CLASSPATH=$CLASSPATH:/waslib/nao_package.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-collections-3.0.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-lang-2.1.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-logging.jar
CLASSPATH=$CLASSPATH:$EXTLIB/commons-dbutils-1.0.jar
CLASSPATH=$CLASSPATH:$EXTLIB/log4j-1.2.8.jar
CLASSPATH=$CLASSPATH:/home/db2inst1/sqllib/java/db2jcc4.jar
CLASSPATH=$CLASSPATH:/home/db2inst1/sqllib/java/db2jcc_license_cu.jar
CLASSPATH=$CLASSPATH:./pg74.216.jdbc2.jar	# PostgreSQL JDBCドライバ
CLASSPATH=$CLASSPATH:./miyagi.jar

# 引数
# DB2_DB=//172.31.120.21:50000/miyadb01
# DB2_USER=db2inst1
# DB2_PW=db2kenja
# PG_DB=//172.31.120.10/renkei
# PG_USER=renkei
# PG_PW=renkei
# IINKAI_DB=//172.31.120.21:50000/iinkaidb
# IINKAI_USER=db2inst1
# IINKAI_PW=db2kenja
# DATE=2012-06-03

# チェック
if [ $# -lt 9 ]; then
    echo "Usage $0: <//db2Host:50000/db2DB> <db2 user> <db2 passwd> <//postgreHost/postgreDB> <postgre user> <postgre passwd> <//iinkaiHost:50000/iinkaiDB> <iinkai user> <iinkai passwd> [yyyy-mm-dd]"
    # echo "Ex1) $0 //172.31.120.21:50000/miyadb01 db2inst1 db2kenja //172.31.120.10/renkei renkei renkei //172.31.120.21:50000/iinkaidb db2inst1 db2kenja"
    # echo "Ex2) $0 //172.31.120.21:50000/miyadb01 db2inst1 db2kenja //172.31.120.10/renkei renkei renkei //172.31.120.21:50000/iinkaidb db2inst1 db2kenja 2016-09-03"
    # TODO: 引数の説明
    exit 1;
else
    DB2_DB=$1
    DB2_USER=$2
    DB2_PW=$3
    PG_DB=$4
    PG_USER=$5
    PG_PW=$6
    IINKAI_DB=$7
    IINKAI_USER=$8
    IINKAI_PW=$9
fi

if [ $# -eq 10 ]; then
    DATE=${10}
else
    DATE=`date '+%Y-%m-%d'`
fi

# グループウェアと連携する
$JAVA_HOME/bin/java -cp $CLASSPATH $MAINCLASS $DB2_DB $DB2_USER "$DB2_PW" $PG_DB $PG_USER "$PG_PW" $IINKAI_DB $IINKAI_USER "$IINKAI_PW" $DATE

# 終了
exit 0;
