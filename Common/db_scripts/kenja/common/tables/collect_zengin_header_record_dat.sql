-- kanji=漢字
-- $Id: d22cc169eea9d904fa72bb33cfa03e24cf0248a4 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

--全銀協フォーマット（ヘッダー・レコード）

drop table COLLECT_ZENGIN_HEADER_RECORD_DAT

create table COLLECT_ZENGIN_HEADER_RECORD_DAT \
( \
        "YEAR"                  varchar(4) not null, \
        "DATA_DIV"              varchar(1) , \
        "TYPE_CD"               varchar(2) , \
        "CD_DIV"                varchar(1) , \
        "CLIENT_CD"             varchar(10), \
        "CLIENT_NAME"           varchar(120), \
        "DIRECT_DEBIT"          varchar(4) not null, \
        "T_BANKCD"              varchar(4) , \
        "T_BANKNAME"            varchar(45), \
        "T_BRANCHCD"            varchar(3) , \
        "T_BRANCHNAME"          varchar(45), \
        "DEPOSIT_DIV"           varchar(1) , \
        "T_ACCOUNTNO"           varchar(7) , \
        "DUMMY"                 varchar(17), \
        "OUTPUT_FLG"            varchar(1) , \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_ZENGIN_HEADER_RECORD_DAT \
add constraint PK_C_ZEN_HED_RC_D \
primary key \
(YEAR, DIRECT_DEBIT)
