-- kanji=漢字
-- $Id: 69f6d09469b86caaff40687571676bbb511ab97d $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計細目マスタ

drop table LEVY_S_MST_OLD
create table LEVY_S_MST_OLD like LEVY_S_MST
insert into LEVY_S_MST_OLD select * from LEVY_S_MST

DROP TABLE LEVY_S_MST \

CREATE TABLE LEVY_S_MST \
( \
        "SCHOOLCD"            varchar(12) not null, \
        "SCHOOL_KIND"         varchar(2)  not null, \
        "YEAR"                varchar(4)  not null, \
        "LEVY_L_CD"           varchar(2) not null, \
        "LEVY_M_CD"           varchar(2) not null, \
        "LEVY_S_CD"           varchar(3) not null, \
        "LEVY_S_NAME"         varchar(90), \
        "LEVY_S_ABBV"         varchar(90), \
        "REPAY_DIV"           varchar(1), \
        "BENEFIT"             varchar(1), \
        "REMARK"              varchar(60), \
        "REGISTERCD"          varchar(10), \
        "UPDATED"             timestamp default current timestamp, \
        "MAX_BENEFIT"         integer \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_S_MST \
ADD CONSTRAINT PK_LEVY_S_MST \
PRIMARY KEY \
(SCHOOLCD, SCHOOL_KIND, YEAR, LEVY_L_CD, LEVY_M_CD, LEVY_S_CD)

insert into LEVY_S_MST \
select \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    LEVY_L_CD, \
    LEVY_M_CD, \
    LEVY_S_CD, \
    LEVY_S_NAME, \
    LEVY_S_ABBV, \
    REPAY_DIV, \
    BENEFIT, \
    REMARK, \
    REGISTERCD, \
    UPDATED, \
    cast(null as integer) as MAX_BENEFIT \
from LEVY_S_MST_OLD
