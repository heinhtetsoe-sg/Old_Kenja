-- $Id: 587d291261fb94a7a648018b6476fd7119a4de99 $

DROP TABLE HREPORT_GUIDANCE_SCHREG_YDAT

CREATE TABLE HREPORT_GUIDANCE_SCHREG_YDAT( \
    YEAR                VARCHAR(4) NOT NULL, \
    SCHREGNO            VARCHAR(8) NOT NULL, \
    CLASSCD             VARCHAR(2) NOT NULL, \
    SCHOOL_KIND         VARCHAR(2) NOT NULL, \
    CURRICULUM_CD       VARCHAR(2) NOT NULL, \
    SUBCLASSCD          VARCHAR(6) NOT NULL, \
    GUIDANCE_PATTERN    VARCHAR(1) NOT NULL, \
    YEAR_TARGET         VARCHAR(608), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HREPORT_GUIDANCE_SCHREG_YDAT ADD CONSTRAINT PK_HREP_GUID_SCHY PRIMARY KEY (YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)