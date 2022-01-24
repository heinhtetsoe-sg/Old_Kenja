-- kanji=����
-- $Id: rep-chair_corres_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ����:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

DROP TABLE CHAIR_CORRES_DAT_OLD
RENAME TABLE CHAIR_CORRES_DAT TO CHAIR_CORRES_DAT_OLD

CREATE TABLE CHAIR_CORRES_DAT \
    ( YEAR           VARCHAR(4) NOT NULL, \
      CHAIRCD        VARCHAR(7) NOT NULL, \
      CLASSCD        VARCHAR(2) NOT NULL, \
      SCHOOL_KIND    VARCHAR(2) NOT NULL, \
      CURRICULUM_CD  VARCHAR(2) NOT NULL, \
      SUBCLASSCD     VARCHAR(6) NOT NULL, \
      REP_SEQ_ALL    SMALLINT, \
      REP_LIMIT      SMALLINT, \
      REP_START_SEQ  SMALLINT, \
      SCH_SEQ_ALL    SMALLINT, \
      SCH_SEQ_MIN    SMALLINT, \
      REGISTERCD     VARCHAR(8), \
      UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CHAIR_CORRES_DAT ADD CONSTRAINT PK_CHR_CORES_DAT PRIMARY KEY (YEAR, CHAIRCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)

INSERT INTO CHAIR_CORRES_DAT \
     SELECT \
         YEAR, \
         CHAIRCD, \
         SUBSTR(SUBCLASSCD, 1, 2) AS CLASSCD, \
         'H' AS SCHOOL_KIND, \
         '2' AS CURRICULUM_CD, \
         SUBCLASSCD, \
         REP_SEQ_ALL, \
         REP_LIMIT, \
         CAST(NULL AS SMALLINT), \
         SCH_SEQ_ALL, \
         SCH_SEQ_MIN, \
         REGISTERCD, \
         UPDATED \
     FROM \
        CHAIR_CORRES_DAT_OLD