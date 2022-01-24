-- kanji=����
-- $Id: c2fa8de8fa7cf833c8a44adb10f18aba40b01028 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table SUPP_EXA_SDIV_DAT

create table SUPP_EXA_SDIV_DAT( \
     YEAR           VARCHAR(4) NOT NULL, \
     SEMESTER       VARCHAR(1) NOT NULL, \
     TESTKINDCD     VARCHAR(2) NOT NULL, \
     TESTITEMCD     VARCHAR(2) NOT NULL, \
     SCORE_DIV      VARCHAR(2) NOT NULL, \
     CLASSCD        VARCHAR(2) NOT NULL, \
     SCHOOL_KIND    VARCHAR(2) NOT NULL, \
     CURRICULUM_CD  VARCHAR(2) NOT NULL, \
     SUBCLASSCD     VARCHAR(6) NOT NULL, \
     SCHREGNO       VARCHAR(8) NOT NULL, \
     SCORE          SMALLINT, \
     SCORE_PASS     SMALLINT, \
     SCORE_PASS_FLG VARCHAR(1), \
     SCORE_FLG      VARCHAR(1), \
     SCORE_DI       VARCHAR(1), \
     REGISTERCD     VARCHAR(10), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table SUPP_EXA_SDIV_DAT add constraint PK_SUPP_EXA_SDIV primary key (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)
