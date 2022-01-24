-- kanji=����
-- $Id: 7299da7d5c488bd12def87b267f93e0deaee7233 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table RECORD_SCORE_BEF_AFT_DAT

create table RECORD_SCORE_BEF_AFT_DAT \
      (YEAR           VARCHAR(4) NOT NULL, \
       SEMESTER       VARCHAR(1) NOT NULL, \
       TESTKINDCD     VARCHAR(2) NOT NULL, \
       TESTITEMCD     VARCHAR(2) NOT NULL, \
       SCORE_DIV      VARCHAR(2) NOT NULL, \
       CLASSCD        VARCHAR(2) NOT NULL, \
       SCHOOL_KIND    VARCHAR(2) NOT NULL, \
       CURRICULUM_CD  VARCHAR(2) NOT NULL, \
       SUBCLASSCD     VARCHAR(6) NOT NULL, \
       SCHREGNO       VARCHAR(8) NOT NULL, \
       CHAIRCD        VARCHAR(7), \
       BEF_SCORE          SMALLINT, \
       BEF_GET_CREDIT     SMALLINT, \
       BEF_COMP_CREDIT    SMALLINT, \
       AFT_SCORE          SMALLINT, \
       AFT_GET_CREDIT     SMALLINT, \
       AFT_COMP_CREDIT    SMALLINT, \
       REGISTERCD     VARCHAR(10), \
       UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table RECORD_SCORE_BEF_AFT_DAT add constraint pk_rec_score_bf_af \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)
