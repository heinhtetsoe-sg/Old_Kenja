-- kanji=漢字
-- $Id: 706e22a41581ac195edc290442b50fabca3e41a8 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--徴収金会計科目マスタ

drop table LEVY_L_MST_OLD
create table LEVY_L_MST_OLD like LEVY_L_MST
insert into LEVY_L_MST_OLD select * from LEVY_L_MST

DROP TABLE LEVY_L_MST \

CREATE TABLE LEVY_L_MST \
( \
        "SCHOOLCD"            varchar(12) not null, \
        "SCHOOL_KIND"         varchar(2)  not null, \
        "YEAR"                varchar(4)  not null, \
        "LEVY_L_CD"           varchar(2) not null, \
        "LEVY_L_NAME"         varchar(90), \
        "LEVY_L_ABBV"         varchar(90), \
        "REGISTERCD"          varchar(10), \
        "UPDATED"             timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LEVY_L_MST ADD CONSTRAINT PK_LEVY_L_MST PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, YEAR, LEVY_L_CD)

insert into LEVY_L_MST \
select \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    LEVY_L_CD, \
    LEVY_L_NAME, \
    LEVY_L_ABBV, \
    REGISTERCD, \
    UPDATED \
from LEVY_L_MST_OLD
