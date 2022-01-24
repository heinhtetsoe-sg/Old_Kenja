-- kanji=����
-- $Id: 885dde3d2023b8530215feba1921ace39ae600cd $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table SUPP_EXA_DAT

create table SUPP_EXA_DAT( \
     YEAR           VARCHAR(4) NOT NULL, \
     SEMESTER       VARCHAR(1) NOT NULL, \
     TESTKINDCD     VARCHAR(2) NOT NULL, \
     TESTITEMCD     VARCHAR(2) NOT NULL, \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     CURRICULUM_CD  VARCHAR(2) NOT NULL, \
     SUBCLASSCD     VARCHAR(6) NOT NULL, \
     SCHREGNO       VARCHAR(8) NOT NULL, \
     SCORE          SMALLINT, \
     SCORE_PASS     SMALLINT, \
     SCORE_FLG      VARCHAR(1), \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table SUPP_EXA_DAT add constraint pk_supp_exa_dt primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)


