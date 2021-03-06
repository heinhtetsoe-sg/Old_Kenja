-- $Id: e1a624d83fc99b34dee6d6792a2c9287de39eb0b $

DROP TABLE HREPORT_CONDITION_DAT
CREATE TABLE HREPORT_CONDITION_DAT( \
    YEAR            VARCHAR(4)      NOT NULL, \
    SCHOOLCD        VARCHAR(12)     NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)      NOT NULL, \
    GRADE           VARCHAR(2)      NOT NULL, \
    COURSECD        VARCHAR(1)      NOT NULL, \
    MAJORCD         VARCHAR(3)      NOT NULL, \
    COURSECODE      VARCHAR(4)      NOT NULL, \
    SEQ             VARCHAR(3)      NOT NULL, \
    REMARK1         VARCHAR(150), \
    REMARK2         VARCHAR(150), \
    REMARK3         VARCHAR(150), \
    REMARK4         VARCHAR(150), \
    REMARK5         VARCHAR(150), \
    REMARK6         VARCHAR(150), \
    REMARK7         VARCHAR(150), \
    REMARK8         VARCHAR(150), \
    REMARK9         VARCHAR(150), \
    REMARK10        VARCHAR(1500), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HREPORT_CONDITION_DAT ADD CONSTRAINT PK_HREPORT_CONDI \
      PRIMARY KEY (YEAR, SCHOOLCD, SCHOOL_KIND, GRADE, COURSECD, MAJORCD, COURSECODE, SEQ)