-- kanji=����
-- $Id: cb0c5cf717861e80372e64b41371c585bf7da011 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

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


