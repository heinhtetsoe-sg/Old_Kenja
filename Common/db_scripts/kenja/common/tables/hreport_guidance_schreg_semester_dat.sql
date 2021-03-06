-- $Id: ee8e706174351f798f5870789866aa177f32adfc $

DROP TABLE HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT

CREATE TABLE HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT( \
    YEAR        VARCHAR(4)     NOT NULL, \
    SEMESTER    VARCHAR(1)     NOT NULL, \
    SCHREGNO    VARCHAR(8)     NOT NULL, \
    SEQ         SMALLINT       NOT NULL, \
    REMARK      VARCHAR(6100) , \
    REGISTERCD  VARCHAR(10)   , \
    UPDATED     TIMESTAMP      DEFAULT CURRENT TIMESTAMP \
 ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE HREPORT_GUIDANCE_SCHREG_SEMESTER_DAT ADD CONSTRAINT PK_HREP_GD_SCH_SMD PRIMARY KEY (YEAR,SEMESTER,SCHREGNO,SEQ)

