-- kanji=漢字
-- $Id: 9c1e9eb027ff3fc5a686ceba77d5d0388ce25e1a $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

DROP TABLE RECORD_PROFICIENCY_ORDER_DAT_OLD
RENAME TABLE RECORD_PROFICIENCY_ORDER_DAT TO RECORD_PROFICIENCY_ORDER_DAT_OLD
CREATE TABLE RECORD_PROFICIENCY_ORDER_DAT( \
     YEAR             varchar(4) not null, \
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

insert into RECORD_PROFICIENCY_ORDER_DAT \
select \
    YEAR, \
    GRADE, \
    SEQ, \
    TEST_DIV, \
    SEMESTER, \
    TESTKINDCD, \
    TESTITEMCD, \
    PROFICIENCY_DIV, \
    PROFICIENCY_CD, \
    REGISTERCD, \
    UPDATED \
from \
    RECORD_PROFICIENCY_ORDER_DAT_OLD

alter table RECORD_PROFICIENCY_ORDER_DAT add constraint PK_REC_PROF_ORDER primary key (YEAR, GRADE, SEQ)
