-- kanji=漢字
-- $Id: rep-college_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback


drop table COLLEGE_MST_OLD

create table COLLEGE_MST_OLD like COLLEGE_MST

insert into COLLEGE_MST_OLD select * from COLLEGE_MST

drop table COLLEGE_MST

create table COLLEGE_MST \
( \
    SCHOOL_CD           char(11) not null, \
    SCHOOL_NAME         varchar(120), \
    BUNAME              varchar(120), \
    KANAME              varchar(120), \
    SCHOOL_SORT         char(2), \
    BUNYA               char(2), \
    AREA_NAME           varchar(30), \
    ZIPCD               varchar(8), \
    ADDR1               varchar(90), \
    ADDR2               varchar(90), \
    TELNO               varchar(16), \
    GREDES              varchar(120), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLEGE_MST add constraint PK_COLLEGE_MST primary key (SCHOOL_CD)

insert into COLLEGE_MST \
    select \
        SUBSTR(CHAR(DECIMAL(SCHOOL_CD,11,0)),1,11) AS SCHOOL_CD, \
        SCHOOL_NAME, \
        BUNAME, \
        KANAME, \
        SCHOOL_SORT, \
        BUNYA, \
        AREA_NAME, \
        ZIPCD, \
        ADDR1, \
        ADDR2, \
        TELNO, \
        GREDES, \
        REGISTERCD, \
        UPDATED \
   from \
        COLLEGE_MST_OLD

