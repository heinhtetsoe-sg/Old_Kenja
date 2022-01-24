-- kanji=漢字
-- $Id: 079cf46be00de43fe9318b743dcf0d33651e12e5 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table PTRN_REFLECTHIST_DAT

create table PTRN_REFLECTHIST_DAT \
( \
    REFLECTDATE    TIMESTAMP not null, \
    REFLECTDIV     VARCHAR (1), \
    SDATE          DATE, \
    EDATE          DATE, \
    YEAR           VARCHAR (4), \
    BSCSEQ         SMALLINT, \
    DAYCD          VARCHAR (1), \
    DAYS           VARCHAR (264), \
    REGISTERCD     VARCHAR (8), \
    UPDATED        TIMESTAMP default current timestamp \
) in usr1dms index in idx1dms

alter table PTRN_REFLECTHIST_DAT add constraint PK_PTRN_REFHIST \
primary key (REFLECTDATE)
