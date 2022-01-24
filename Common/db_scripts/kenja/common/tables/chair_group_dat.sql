-- kanji=漢字
-- $Id: 02c943a0eedf0c464fe759e73e31fd6ca7a0146d $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table CHAIR_GROUP_DAT

create table CHAIR_GROUP_DAT \
(  \
        "YEAR"              varchar(4) not null, \
        "SEMESTER"          varchar(1) not null, \
        "CHAIR_GROUP_CD"    varchar(6) not null, \
        "TESTKINDCD"        varchar(2) not null, \
        "TESTITEMCD"        varchar(2) not null, \
        "CHAIRCD"           varchar(7) not null, \
        "REGISTERCD"        varchar(8), \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CHAIR_GROUP_DAT  \
add constraint PK_CHAIR_GROUP_DAT \
primary key  \
(YEAR, SEMESTER, CHAIR_GROUP_CD, TESTKINDCD, TESTITEMCD, CHAIRCD)
