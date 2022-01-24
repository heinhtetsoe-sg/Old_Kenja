-- $Id: 818c44214f777a3e73ba266bf1613af57ad44dc6 $

DROP TABLE SETTING_DAT
CREATE TABLE SETTING_DAT( \
    SCHOOLCD        VARCHAR(12)     NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)      NOT NULL, \
    SEQ             VARCHAR(3)      NOT NULL, \
    SEQMEMO         VARCHAR(60), \
    REMARK1         VARCHAR(150), \
    REMARK2         VARCHAR(150), \
    REMARK3         VARCHAR(150), \
    REMARK4         VARCHAR(150), \
    REMARK5         VARCHAR(150), \
    REMARK6         VARCHAR(150), \
    REMARK7         VARCHAR(150), \
    REMARK8         VARCHAR(150), \
    REMARK9         VARCHAR(150), \
    REMARK10        VARCHAR(150), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SETTING_DAT ADD CONSTRAINT PK_SETTING_DAT \
      PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, SEQ)