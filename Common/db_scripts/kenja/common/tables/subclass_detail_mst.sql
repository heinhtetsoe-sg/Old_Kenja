-- $Id: e3da320ca02791abd6f2c25428007a0d7729dadc $

DROP TABLE SUBCLASS_DETAIL_MST
CREATE TABLE SUBCLASS_DETAIL_MST( \
    CLASSCD             VARCHAR(2) NOT NULL, \
    SCHOOL_KIND         VARCHAR(2) NOT NULL, \
    CURRICULUM_CD       VARCHAR(2) NOT NULL, \
    SUBCLASSCD          VARCHAR(6) NOT NULL, \
    SUBCLASS_SEQ        VARCHAR(3) NOT NULL, \
    SUBCLASS_REMARK1    VARCHAR(30), \
    SUBCLASS_REMARK2    VARCHAR(30), \
    SUBCLASS_REMARK3    VARCHAR(30), \
    SUBCLASS_REMARK4    VARCHAR(30), \
    SUBCLASS_REMARK5    VARCHAR(30), \
    SUBCLASS_REMARK6    VARCHAR(30), \
    SUBCLASS_REMARK7    VARCHAR(30), \
    SUBCLASS_REMARK8    VARCHAR(30), \
    SUBCLASS_REMARK9    VARCHAR(30), \
    SUBCLASS_REMARK10   VARCHAR(30), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUBCLASS_DETAIL_MST ADD CONSTRAINT PK_SUBCLASS_DETA PRIMARY KEY (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SUBCLASS_SEQ)