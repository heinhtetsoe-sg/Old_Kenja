-- kanji=����
-- $Id: 5de5e9417f02971d1911b54aef017c3877f69b66 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table RECORD_MOCK_RANK_DAT

create table RECORD_MOCK_RANK_DAT( \
       YEAR           VARCHAR(4) NOT NULL, \
       SCHREGNO       VARCHAR(8) NOT NULL, \
       DATA_DIV       VARCHAR(1) NOT NULL, \
       COURSE_DIV     VARCHAR(1) NOT NULL, \
       CLASSCD        VARCHAR(2) NOT NULL, \
       SCHOOL_KIND    VARCHAR(2) NOT NULL, \
       CURRICULUM_CD  VARCHAR(2) NOT NULL, \
       SUBCLASSCD     VARCHAR(6) NOT NULL, \
       GRADE          VARCHAR(2) NOT NULL, \
       SCORE1         SMALLINT, \
       SCORE2         SMALLINT, \
       SCORE3         SMALLINT, \
       RANK1          SMALLINT, \
       RANK2          SMALLINT, \
       RANK           SMALLINT, \
       PERFECT1       SMALLINT, \
       PERFECT2       SMALLINT, \
       PERFECT3       SMALLINT, \
       REGISTERCD     VARCHAR(8), \
       UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table RECORD_MOCK_RANK_DAT add constraint pk_rec_mock_rank \
      primary key (YEAR, SCHREGNO, DATA_DIV, COURSE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
