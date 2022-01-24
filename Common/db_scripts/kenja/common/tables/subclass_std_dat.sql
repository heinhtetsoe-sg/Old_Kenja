-- kanji=����
-- $Id: ad5c0a7df84885cd06d070008247fbdbc9aa16c1 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

DROP TABLE SUBCLASS_STD_DAT

CREATE TABLE SUBCLASS_STD_DAT \
      (YEAR          VARCHAR(4)      NOT NULL, \
       SEMESTER      VARCHAR(1)      NOT NULL, \
       CLASSCD       VARCHAR(2)      NOT NULL, \
       CURRICULUM_CD VARCHAR(1)      NOT NULL, \
       SUBCLASSCD    VARCHAR(6)      NOT NULL, \
       SCHREGNO      VARCHAR(8)      NOT NULL, \
       REGISTERCD    VARCHAR(8), \
       UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUBCLASS_STD_DAT ADD CONSTRAINT PK_SBCLS_STD_DAT PRIMARY KEY \
      (YEAR,SEMESTER,CLASSCD,CURRICULUM_CD,SUBCLASSCD,SCHREGNO)

