-- kanji=����
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table CHAIR_ATTENTION_SCORE_MST

create table CHAIR_ATTENTION_SCORE_MST ( \
    YEAR            varchar(4) not null, \
    SEMESTER        varchar(1) not null, \
    TESTKINDCD      varchar(2) not null, \
    TESTITEMCD      varchar(2) not null, \
    SCORE_DIV       varchar(2) not null, \
    CLASSCD         varchar(2) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    CURRICULUM_CD   varchar(2) not null, \
    SUBCLASSCD      varchar(6) not null, \
    CHAIRCD         varchar(7) not null, \
    ATTENTION_SCORE smallint, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CHAIR_ATTENTION_SCORE_MST add constraint PK_CHAIR_ATTENTION_SCORE_MST \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD)
