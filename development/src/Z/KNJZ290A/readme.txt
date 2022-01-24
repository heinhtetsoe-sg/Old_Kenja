# kanji=漢字
# $Id: readme.txt 56591 2017-10-22 13:04:39Z maeshiro $

/** 導入時に必要なプログラム等 **/
# STAFFの履歴関連(教育委員会と同等)
# に必要な設定等
# ★Query.php
#   関数：dbCheckOut2が入っている事
# ★gk.conf(項目追加)
#   Database2 = ALPOKIDB
#   User2     = db2inst1
#   Password2 = db2inst1
# ★prepend.inc(項目追加)
#   define("DB_DATABASE2", $objIni->param("database","Database2"));
#   define("DB_USER2"    , $objIni->param("database","User2"));
#   define("DB_PASSWORD2", $objIni->param("database","Password2"));
#   define("DSN2"        , PHPTYPE."://".DB_USER2.":".DB_PASSWORD2." @".DB_DATABASE2 );
# ★スクリプト
#   edboard*.sql
#   staff_address_dat.sql
#   staff_another_dat.sql
#   staff_detail_mst.sql
#   staff_name_hist_dat.sql
#   staff_qualified_dat.sql
#   staff_requestform_dat.sql
#   staff_principal_hist_dat.sql
#   staff_class_mst.sql


2014/06/11  1.新規作成(KNJZ291Aを元に)
