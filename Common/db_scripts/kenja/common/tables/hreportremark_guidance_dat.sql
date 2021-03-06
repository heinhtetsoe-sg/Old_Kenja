-- $Id: 1cd6459e4138b108a0e143f6ae114582fabaa744 $

DROP TABLE HREPORTREMARK_GUIDANCE_DAT

CREATE TABLE HREPORTREMARK_GUIDANCE_DAT( \
    YEAR                VARCHAR(4) NOT NULL, \
    SEMESTER            VARCHAR(1) NOT NULL, \
    SCHREGNO            VARCHAR(8) NOT NULL, \
    CLASSCD             VARCHAR(2) NOT NULL, \
    SCHOOL_KIND         VARCHAR(2) NOT NULL, \
    CURRICULUM_CD       VARCHAR(2) NOT NULL, \
    SUBCLASSCD          VARCHAR(6) NOT NULL, \
    REMARK1             VARCHAR(1000), \
    REMARK2             VARCHAR(1000), \
    REMARK3             VARCHAR(1000), \
    REMARK4             VARCHAR(1000), \
    REMARK5             VARCHAR(1000), \
    REMARK6             VARCHAR(1000), \
    REMARK7             VARCHAR(1000), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HREPORTREMARK_GUIDANCE_DAT ADD CONSTRAINT PK_HREPORTR_GUID PRIMARY KEY (YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
