-- kanji=����
-- $Id: be3b25e337044cad94a0edcf5786fe9b93c79b11 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

DROP TABLE SUBCLASS_HOLD_DAT

CREATE TABLE SUBCLASS_HOLD_DAT( \
       ENTYEAR       VARCHAR(4)      NOT NULL, \
       COURSECD      VARCHAR(1)      NOT NULL, \
       MAJORCD       VARCHAR(3)      NOT NULL, \
       CLASSCD       VARCHAR(2)      NOT NULL, \
       SCHOOL_KIND   VARCHAR(2)      NOT NULL, \
       CURRICULUM_CD VARCHAR(2)      NOT NULL, \
       SUBCLASSCD    VARCHAR(6)      NOT NULL, \
       SELECTKIND    VARCHAR(2)      NOT NULL, \
       STUDY1        VARCHAR(1), \
       STUDY2        VARCHAR(1), \
       STUDY3        VARCHAR(1), \
       STUDY4        VARCHAR(1), \
       STUDY5        VARCHAR(1), \
       STUDY6        VARCHAR(1), \
       CREDITS       smallint, \
       REMARK        VARCHAR(75), \
       REGISTERCD    VARCHAR(10), \
       UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUBCLASS_HOLD_DAT ADD CONSTRAINT PK_SBCLS_HOLD_DAT PRIMARY KEY \
      (ENTYEAR,COURSECD,MAJORCD,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)

