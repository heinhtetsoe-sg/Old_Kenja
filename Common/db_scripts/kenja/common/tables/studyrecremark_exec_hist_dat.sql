-- kanji=漢字
-- $Id: 0257c53eef2e7b9516db5a2429ce278fb8e7e72f $

DROP TABLE STUDYRECREMARK_EXEC_HIST_DAT
CREATE TABLE STUDYRECREMARK_EXEC_HIST_DAT( \
    CALC_DATE       DATE NOT NULL, \
    CALC_TIME       TIME NOT NULL, \
    BEF_AFT_DIV     VARCHAR(1) NOT NULL, \
    YEAR            VARCHAR(4) NOT NULL, \
    SCHREGNO        VARCHAR(8) NOT NULL, \
    CLASSCD         VARCHAR(2) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2) NOT NULL, \
    CURRICULUM_CD   VARCHAR(2) NOT NULL, \
    SUBCLASSCD      VARCHAR(6) NOT NULL, \
    REMARK          VARCHAR(150), \
    CMD_DIV         VARCHAR(1), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STUDYRECREMARK_EXEC_HIST_DAT ADD CONSTRAINT PK_STUDYRECREMARK_EXEC_HIST_DAT PRIMARY KEY (CALC_DATE,CALC_TIME,BEF_AFT_DIV,YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)
