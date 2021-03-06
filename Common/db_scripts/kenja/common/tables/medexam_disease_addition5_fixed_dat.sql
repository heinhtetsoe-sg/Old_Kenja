-- kanji=????
-- $Id:

DROP TABLE MEDEXAM_DISEASE_ADDITION5_FIXED_DAT

CREATE TABLE MEDEXAM_DISEASE_ADDITION5_FIXED_DAT( \
    EDBOARD_SCHOOLCD VARCHAR(12)   NOT NULL, \
    SHORI_CD         VARCHAR(2)    NOT NULL, \
    YEAR             VARCHAR(4)    NOT NULL, \
    FIXED_DATE       date not null, \
    GRADE            VARCHAR(2)    NOT NULL, \
    SEX              VARCHAR(1)    NOT NULL, \
    REMARK_DIV       VARCHAR(3)    NOT NULL, \
    COUNT            INTEGER, \
    REGISTERCD       VARCHAR(10), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MEDEXAM_DISEASE_ADDITION5_FIXED_DAT ADD CONSTRAINT PK_MEDEXAM_D_A5_F PRIMARY KEY (EDBOARD_SCHOOLCD, SHORI_CD, YEAR, FIXED_DATE, GRADE, SEX, REMARK_DIV)