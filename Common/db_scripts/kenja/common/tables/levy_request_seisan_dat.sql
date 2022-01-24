-- kanji=漢字
-- $Id: 46e9b452cbb8900a3d0bbef1abf38093f634ecea $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金清算書データ

drop table LEVY_REQUEST_SEISAN_DAT

create table LEVY_REQUEST_SEISAN_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "SEISAN_L_CD"           varchar(2)  not null, \
        "SEISAN_M_CD"           varchar(2)  not null, \
        "REQUEST_NO"            varchar(10) not null, \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "SEISAN_TITLE"          varchar(120), \
        "GENKIN_JURYOU_STAFF"   varchar(10), \
        "SEISAN_NAIYOU"         varchar(250), \
        "JURYOU_GK"             integer, \
        "JURYOU_DATE"           date, \
        "SIHARAI_GK"            integer, \
        "SIHARAI_DATE"          date, \
        "ZAN_GK"                integer, \
        "REMARK"                varchar(250), \
        "SEISAN_APPROVAL"       varchar(1), \
        "SUITOU_STAFF"          varchar(10), \
        "INCOME_DATE"           date, \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_SEISAN_DAT add constraint PK_LEVY_SEISAN_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SEISAN_L_CD, SEISAN_M_CD, REQUEST_NO)
