-- $Id: bb11e5a4a73b79a6ce3c2b087e81e84239185c5d $

DROP TABLE SCHREG_REGD_GHR_DAT

CREATE TABLE SCHREG_REGD_GHR_DAT( \
    SCHREGNO            VARCHAR(8)      NOT NULL, \
    YEAR                VARCHAR(4)      NOT NULL, \
    SEMESTER            VARCHAR(1)      NOT NULL, \
    GHR_CD              VARCHAR(2), \
    GHR_ATTENDNO        VARCHAR(3), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_REGD_GHR_DAT ADD CONSTRAINT PK_SCHRG_RGD_GHR_D PRIMARY KEY (SCHREGNO, YEAR, SEMESTER)
