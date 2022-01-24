-- kanji=漢字
-- $Id: c52c6995bbe69f4eda30524e24acc0b88a1974e1 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金施工伺いデータ

drop table LEVY_REQUEST_YOSAN_DAT

create table LEVY_REQUEST_YOSAN_DAT \
( \
        "SCHOOLCD"              varchar(12) not null, \
        "SCHOOL_KIND"           varchar(2)  not null, \
        "YEAR"                  varchar(4)  not null, \
        "YOSAN_DIV"             varchar(2)  not null, \
        "YOSAN_L_CD"            varchar(2)  not null, \
        "YOSAN_M_CD"            varchar(2)  not null, \
        "REQUEST_NO"            varchar(10), \
        "REQUEST_DATE"          date, \
        "REQUEST_REASON"        varchar(120), \
        "REQUEST_STAFF"         varchar(10), \
        "REQUEST_GK"            integer, \
        "REQUEST_TESUURYOU"     integer, \
        "REMARK"                varchar(500), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_YOSAN_DAT add constraint PK_LEVY_YOSAN_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, YOSAN_DIV, YOSAN_L_CD, YOSAN_M_CD)
