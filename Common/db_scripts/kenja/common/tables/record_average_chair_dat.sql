-- kanji=����
-- $Id: fd1bbbe856326f0d221002be58d514963416338d $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table RECORD_AVERAGE_CHAIR_DAT

create table RECORD_AVERAGE_CHAIR_DAT ( \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    TESTKINDCD      VARCHAR(2) NOT NULL, \
    TESTITEMCD      VARCHAR(2) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    CHAIRCD         VARCHAR(7) NOT NULL, \
    AVG_DIV         VARCHAR(1) NOT NULL, \
    GRADE           VARCHAR(2) NOT NULL, \
    HR_CLASS        VARCHAR(3) NOT NULL, \
    COURSECD        VARCHAR(1) NOT NULL, \
    MAJORCD         VARCHAR(3) NOT NULL, \
    COURSECODE      VARCHAR(4) NOT NULL, \
    SCORE           INTEGER, \
    HIGHSCORE       INTEGER, \
    LOWSCORE        INTEGER, \
    COUNT           SMALLINT, \
    AVG             DECIMAL (9,5), \
    STDDEV          DECIMAL (5,1), \
    CHAIRDATE       DATE, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECORD_AVERAGE_CHAIR_DAT add constraint pk_rec_avg_chr_dat \
      primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, CHAIRCD, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
