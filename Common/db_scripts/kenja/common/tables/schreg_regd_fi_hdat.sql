-- $Id: eb14925d6834dd603909265cf0adee2630f5575f $

DROP TABLE SCHREG_REGD_FI_HDAT
CREATE TABLE SCHREG_REGD_FI_HDAT \
      (YEAR              VARCHAR(4)      NOT NULL, \
       SEMESTER          VARCHAR(1)      NOT NULL, \
       GRADE             VARCHAR(2)      NOT NULL, \
       HR_CLASS          VARCHAR(3)      NOT NULL, \
       RECORD_DIV        VARCHAR(1)      NOT NULL, \
       HR_NAME           VARCHAR(15), \
       HR_NAMEABBV       VARCHAR(5), \
       GRADE_NAME        VARCHAR(30), \
       HR_CLASS_NAME1    VARCHAR(30), \
       HR_CLASS_NAME2    VARCHAR(30), \
       HR_FACCD          VARCHAR(4), \
       TR_CD1            VARCHAR(8), \
       TR_CD2            VARCHAR(8), \
       TR_CD3            VARCHAR(8), \
       SUBTR_CD1         VARCHAR(8), \
       SUBTR_CD2         VARCHAR(8), \
       SUBTR_CD3         VARCHAR(8), \
       CLASSWEEKS        SMALLINT, \
       CLASSDAYS         SMALLINT, \
       REGISTERCD        VARCHAR(8), \
       UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_REGD_FI_HDAT ADD CONSTRAINT PK_SCH_R_FI_HDAT PRIMARY KEY (YEAR,SEMESTER,GRADE,HR_CLASS)
