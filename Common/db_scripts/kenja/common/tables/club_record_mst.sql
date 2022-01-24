-- kanji=漢字
-- $Id: 20a160a98bf7ce3769ae77d134fcde626b58ca10 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CLUB_RECORD_MST

create table CLUB_RECORD_MST \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    RECORDCD       VARCHAR (3) not null, \
    RECORDNAME     VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_RECORD_MST add constraint PK_CLUB_REC_MST \
primary key (SCHOOLCD, SCHOOL_KIND, RECORDCD)
