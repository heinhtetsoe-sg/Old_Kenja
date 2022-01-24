-- $Id: 11a7181986cd960a367ebc7e5aff90a03f464e98 $

DROP TABLE SCHREG_REGD_GHR_HDAT_OLD
RENAME TABLE SCHREG_REGD_GHR_HDAT TO SCHREG_REGD_GHR_HDAT_OLD
CREATE TABLE SCHREG_REGD_GHR_HDAT( \
    YEAR                VARCHAR(4)      NOT NULL, \
    SEMESTER            VARCHAR(1)      NOT NULL, \
    GHR_CD              VARCHAR(2)      NOT NULL, \
    GHR_NAME            VARCHAR(30), \
    GHR_NAMEABBV        VARCHAR(30), \
    TR_CD1              VARCHAR(8), \
    TR_CD2              VARCHAR(8), \
    TR_CD3              VARCHAR(8), \
    SUBTR_CD1           VARCHAR(8), \
    SUBTR_CD2           VARCHAR(8), \
    SUBTR_CD3           VARCHAR(8), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO SCHREG_REGD_GHR_HDAT \
    SELECT \
        YEAR, \
        SEMESTER, \
        GHR_CD, \
        GHR_NAME, \
        GHR_NAMEABBV, \
        TR_CD1, \
        TR_CD2, \
        TR_CD3, \
        SUBTR_CD1, \
        SUBTR_CD2, \
        SUBTR_CD3, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SCHREG_REGD_GHR_HDAT_OLD

ALTER TABLE SCHREG_REGD_GHR_HDAT ADD CONSTRAINT PK_SCHRG_RGD_GHR_H PRIMARY KEY (YEAR, SEMESTER, GHR_CD)