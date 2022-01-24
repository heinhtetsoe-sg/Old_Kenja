-- kanji=漢字
-- $Id: 68188549e82d9bbc46e192dd001d30268ec8a641 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金収入伺いデータ

drop table LEVY_REQUEST_INCOME_DAT_OLD
create table LEVY_REQUEST_INCOME_DAT_OLD like LEVY_REQUEST_INCOME_DAT
insert into LEVY_REQUEST_INCOME_DAT_OLD select * from LEVY_REQUEST_INCOME_DAT

drop table LEVY_REQUEST_INCOME_DAT

create table LEVY_REQUEST_INCOME_DAT \
( \
        "SCHOOLCD"                  varchar(12) not null, \
        "SCHOOL_KIND"               varchar(2)  not null, \
        "YEAR"                      varchar(4)  not null, \
        "INCOME_L_CD"               varchar(2)  not null, \
        "INCOME_M_CD"               varchar(2)  not null, \
        "REQUEST_NO"                varchar(10) not null, \
        "REQUEST_DATE"              date, \
        "REQUEST_REASON"            varchar(120), \
        "REQUEST_STAFF"             varchar(10), \
        "REQUEST_GK"                int, \
        "COLLECT_DIV"               varchar(1) not null, \
        "COLLECT_WARIHURI_DIV"      varchar(1), \
        "COLLECT_L_CD"              varchar(2), \
        "COLLECT_M_CD"              varchar(2), \
        "COLLECT_S_CD"              varchar(3), \
        "INCOME_APPROVAL"           varchar(1), \
        "INCOME_CANCEL"             varchar(1), \
        "INCOME_DIV"                varchar(2), \
        "INCOME_DATE"               date, \
        "INCOME_NO"                 varchar(10), \
        "INCOME_STAFF"              varchar(10), \
        "INCOME_CERTIFICATE_CNT"    varchar(10), \
        "FROM_SCHOOL_KIND"          varchar(2), \
        "REGISTERCD"                varchar(10), \
        "UPDATED"                   timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table LEVY_REQUEST_INCOME_DAT add constraint PK_LEVY_REQ_INCOME primary key (SCHOOLCD, SCHOOL_KIND, YEAR, INCOME_L_CD, INCOME_M_CD, REQUEST_NO)

insert into LEVY_REQUEST_INCOME_DAT \
select \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    INCOME_L_CD, \
    INCOME_M_CD, \
    REQUEST_NO, \
    REQUEST_DATE, \
    REQUEST_REASON, \
    REQUEST_STAFF, \
    REQUEST_GK, \
    COLLECT_DIV, \
    COLLECT_WARIHURI_DIV, \
    COLLECT_L_CD, \
    COLLECT_M_CD, \
    COLLECT_S_CD, \
    INCOME_APPROVAL, \
    INCOME_CANCEL, \
    INCOME_DIV, \
    INCOME_DATE, \
    INCOME_NO, \
    INCOME_STAFF, \
    INCOME_CERTIFICATE_CNT, \
    cast(null as varchar(2)) as FROM_SCHOOL_KIND, \
    REGISTERCD, \
    UPDATED \
from LEVY_REQUEST_INCOME_DAT_OLD
