-- kanji=漢字
-- $Id$

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CHAIR_GROUP_SDIV_DAT

create table CHAIR_GROUP_SDIV_DAT \
(  \
        "YEAR"              varchar(4) not null, \
        "SEMESTER"          varchar(1) not null, \
        "CHAIR_GROUP_CD"    varchar(6) not null, \
        "TESTKINDCD"        varchar(2) not null, \
        "TESTITEMCD"        varchar(2) not null, \
        "SCORE_DIV"         varchar(2) not null, \
        "CHAIRCD"           varchar(7) not null, \
        "REGISTERCD"        varchar(8), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CHAIR_GROUP_SDIV_DAT  \
add constraint PK_CHAIR_GROUP_SDIV_DAT \
primary key  \
(YEAR, SEMESTER, CHAIR_GROUP_CD, TESTKINDCD, TESTITEMCD, SCORE_DIV, CHAIRCD)
