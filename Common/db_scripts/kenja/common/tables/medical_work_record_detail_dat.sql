-- $Id: 7d8c91808e16806875c3677fd877520a89a9b45c $

DROP TABLE MEDICAL_WORK_RECORD_DETAIL_DAT
CREATE TABLE MEDICAL_WORK_RECORD_DETAIL_DAT( \
    WORK_DATE               DATE    NOT NULL, \
    STAFFCD                 VARCHAR(8)    NOT NULL, \
    WORK_DIV                VARCHAR(1)    NOT NULL, \
    SEQ                     VARCHAR(3)    NOT NULL, \
    REMARK1                 VARCHAR(90), \
    REMARK2                 VARCHAR(90), \
    REMARK3                 VARCHAR(90), \
    REMARK4                 VARCHAR(90), \
    REMARK5                 VARCHAR(90), \
    REMARK6                 VARCHAR(90), \
    REMARK7                 VARCHAR(90), \
    REMARK8                 VARCHAR(90), \
    REMARK9                 VARCHAR(90), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MEDICAL_WORK_RECORD_DETAIL_DAT ADD CONSTRAINT PK_MED_WORK_D_REC PRIMARY KEY (WORK_DATE, STAFFCD, WORK_DIV, SEQ)