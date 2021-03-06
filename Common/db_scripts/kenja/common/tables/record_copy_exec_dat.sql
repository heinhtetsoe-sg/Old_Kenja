-- kanji=漢字
-- $Id: e4c2a5f021fc83482c6565f30a5a087134c99ad4 $

DROP TABLE RECORD_COPY_EXEC_DAT

CREATE TABLE RECORD_COPY_EXEC_DAT( \
    CALC_DATE       DATE NOT NULL, \
    CALC_TIME       TIME NOT NULL, \
    PROGRAMID       VARCHAR(30), \
    YEAR            VARCHAR(4), \
    SEMESTER        VARCHAR(1), \
    SELECT_HR_CLASS VARCHAR(500), \
    SELECT_SUBCLASS VARCHAR(500), \
    SHORI_DIV       VARCHAR(1), \
    SAKI_TESTCD     VARCHAR(7), \
    MOTO_TESTCD     VARCHAR(7), \
    KARI_DIV        VARCHAR(1), \
    KARI_TESTCD     VARCHAR(7), \
    SOUGAKU_FLG     VARCHAR(2), \
    CONVERT_FLG     VARCHAR(2), \
    KEEKA_OVER_FLG  VARCHAR(2), \
    SIDOU_FLG       VARCHAR(2), \
    DELETE_FLG      VARCHAR(1), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE RECORD_COPY_EXEC_DAT ADD CONSTRAINT PK_REC_COPY_EXE_DT PRIMARY KEY (CALC_DATE,CALC_TIME)