-- kanji=漢字
-- $Id: 0318796568e33fb3334a5c118c7ed16e6fdd30a3 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目中分類マスタ
drop table COLLECT_M_DETAIL_MST

create table COLLECT_M_DETAIL_MST \
( \
        "SCHOOLCD"            varchar(12) not null, \
        "SCHOOL_KIND"         varchar(2)  not null, \
        "YEAR"                varchar(4)  not null, \
        "COLLECT_L_CD"        varchar(2)  not null, \
        "COLLECT_M_CD"        varchar(2)  not null, \
        "SEQ"                 varchar(3)  not null, \
        "INT_VAL1"            integer, \
        "INT_VAL2"            integer, \
        "INT_VAL3"            integer, \
        "REMARK1"             varchar(90), \
        "REMARK2"             varchar(90), \
        "REMARK3"             varchar(90), \
        "REMARK4"             varchar(90), \
        "REMARK5"             varchar(90), \
        "REGISTERCD"          varchar(10), \
        "UPDATED"             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_M_DETAIL_MST \
add constraint PK_COL_M_DETAIL_M \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_L_CD, COLLECT_M_CD, SEQ)
