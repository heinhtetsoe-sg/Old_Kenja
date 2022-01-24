-- kanji=����
-- $Id: e2660bf79f3dc3e8a7f5d6167c5c78dd7ce1cdd5 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table RECORD_PROV_RATE_DAT

create table RECORD_PROV_RATE_DAT \
      (YEAR           VARCHAR(4) NOT NULL, \
       SEMESTER       VARCHAR(1) NOT NULL, \
       TESTKINDCD     VARCHAR(2) NOT NULL, \
       TESTITEMCD     VARCHAR(2) NOT NULL, \
       CLASSCD        VARCHAR(2) NOT NULL, \
       SCHOOL_KIND    VARCHAR(2) NOT NULL, \
       CURRICULUM_CD  VARCHAR(2) NOT NULL, \
       SUBCLASSCD     VARCHAR(6) NOT NULL, \
       SCHREGNO       VARCHAR(8) NOT NULL, \
       VALUE          SMALLINT, \
       GET_CREDIT     SMALLINT, \
       COMP_CREDIT    SMALLINT, \
       REGISTERCD     VARCHAR(8), \
       UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table RECORD_PROV_RATE_DAT add constraint pk_rec_pro_rate_dt \
      primary key (YEAR,SEMESTER,TESTKINDCD,TESTITEMCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)
