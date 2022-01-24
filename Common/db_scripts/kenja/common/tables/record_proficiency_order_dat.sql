-- kanji=漢字
-- $Id: cb0c5cf717861e80372e64b41371c585bf7da011 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

drop table RECORD_PROFICIENCY_ORDER_DAT

create table RECORD_PROFICIENCY_ORDER_DAT \
    (YEAR             varchar(4) not null, \
     GRADE            varchar(2) not null, \
     SEQ              smallint   not null, \
     TEST_DIV         varchar(1) not null, \
     SEMESTER         varchar(1), \
     TESTKINDCD       varchar(2), \
     TESTITEMCD       varchar(2), \
     PROFICIENCYDIV   varchar(2), \
     PROFICIENCYCD    varchar(4), \
     REGISTERCD       varchar(8), \
     UPDATED          timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table RECORD_PROFICIENCY_ORDER_DAT add constraint PK_REC_PROF_ORDER primary key (YEAR, GRADE, SEQ)


