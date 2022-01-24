-- kanji=漢字
-- $Id: rep-company_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


drop table COMPANY_MST_OLD

create table COMPANY_MST_OLD like COMPANY_MST

insert into COMPANY_MST_OLD select * from COMPANY_MST

drop table COMPANY_MST

create table COMPANY_MST ( \
    COMPANY_CD          char(11) not null, \
    COMPANY_NAME        varchar(120), \
    SHUSHOKU_ADDR       varchar(120), \
    SHIHONKIN           varchar(17), \
    SONINZU             int, \
    TONINZU             int, \
    COMPANY_SORT        char(2), \
    TARGET_SEX          char(1), \
    ZIPCD               varchar(8), \
    ADDR1               varchar(90), \
    ADDR2               varchar(90), \
    TELNO               varchar(16), \
    REMARK              varchar(120), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COMPANY_MST add constraint PK_COMPANY_MST primary key (COMPANY_CD)

insert into COMPANY_MST \
    select \
        SUBSTR(CHAR(DECIMAL(COMPANY_CD,11,0)),1,11) AS COMPANY_CD, \
        COMPANY_NAME, \
        SHUSHOKU_ADDR, \
        SHIHONKIN, \
        SONINZU, \
        TONINZU, \
        COMPANY_SORT, \
        TARGET_SEX, \
        ZIPCD, \
        ADDR1, \
        ADDR2, \
        TELNO, \
        REMARK, \
        REGISTERCD, \
        UPDATED \
   from \
        COMPANY_MST_OLD

