-- $Id$

DROP TABLE SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT
CREATE TABLE SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT( \
    SCHREGNO                            VARCHAR(8)    NOT NULL, \
    RECORD_DATE                         VARCHAR(10)   NOT NULL, \
    RECORD_DIV                          VARCHAR(1)    NOT NULL, \
    RECORD_SEQ                          SMALLINT      NOT NULL, \
    MEDICINE_NAME                       VARCHAR(50), \
    DISEASE_CONDITION_NAME              VARCHAR(150), \
    CARE_WAY                            VARCHAR(500), \
    SCHOOL_CARE                         VARCHAR(105), \
    HOUSE_CARE                          VARCHAR(105), \
    CENTER_CARE                         VARCHAR(105), \
    ALLERGIA_FOOD_CAT                   VARCHAR(105) , \
    ALLERGIA_PLAN                       VARCHAR(120) , \
    ALLERGIA_SPECIAL_REPORT             VARCHAR(120) , \
    ALLERGIA_FOOD_STYLE                 VARCHAR(120) , \
    ALLERGIA_REMARK                     VARCHAR(120) , \
    REMARK1                             VARCHAR(500) , \
    REMARK2                             VARCHAR(500) , \
    REMARK3                             VARCHAR(500) , \
    REMARK4                             VARCHAR(1500), \
    REMARK5                             VARCHAR(500) , \
    REMARK6                             VARCHAR(500) , \
    REMARK7                             VARCHAR(500) , \
    REMARK8                             VARCHAR(500) , \
    REMARK9                             VARCHAR(500) , \
    REMARK10                            VARCHAR(500) , \
    REGISTERCD                          VARCHAR(8), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN usr16dms index in idx1dms

ALTER TABLE SCHREG_CHALLENGED_PROFILE_HEALTHCARE_RECORD_DAT ADD CONSTRAINT PK_SCH_CHA_P_H_R_D PRIMARY KEY (SCHREGNO, RECORD_DATE, RECORD_DIV, RECORD_SEQ)
