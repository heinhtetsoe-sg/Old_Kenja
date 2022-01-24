-- kanji=漢字
-- $Id: e1ee3a9c9c1993b8366b707c19fbc534d47bf73a $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--費目中分類マスタ

drop table COLLECT_M_DOMITORY_MST

create table COLLECT_M_DOMITORY_MST \
( \
        DOMI_CD             varchar(3)  not null, \
        SCHOOLCD            varchar(12) not null, \
        SCHOOL_KIND         varchar(2)  not null, \
        YEAR                varchar(4)  not null, \
        COLLECT_L_CD        varchar(2)  not null, \
        COLLECT_M_CD        varchar(2)  not null, \
        MONTH_MONEY         int,  \
        DAY_MONEY           int,  \
        REGISTERCD          varchar(10), \
        UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_M_DOMITORY_MST \
add constraint PK_COL_M_DOMITORY \
primary key \
(DOMI_CD)
