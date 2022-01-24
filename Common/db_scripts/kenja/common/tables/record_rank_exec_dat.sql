-- kanji=漢字
-- $Id: 80178b2f952b3f5ad8ec335537743b6014fa4648 $

DROP TABLE RECORD_RANK_EXEC_DAT

CREATE TABLE RECORD_RANK_EXEC_DAT( \
    CALC_DATE     DATE          NOT NULL, \
    CALC_TIME     TIME          NOT NULL, \
    CLASSCD       VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND   VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD VARCHAR(2)    NOT NULL, \
    SUBCLASSCD    VARCHAR(6)    NOT NULL, \
    YEAR          VARCHAR(4), \
    SEMESTER      VARCHAR(1), \
    TESTKINDCD    VARCHAR(2), \
    TESTITEMCD    VARCHAR(2), \
    GRADE         VARCHAR(2), \
    CHAIRDATE     DATE, \
    ELECTDIV_FLG  VARCHAR(1), \
    REGISTERCD    VARCHAR(8), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE RECORD_RANK_EXEC_DAT ADD CONSTRAINT PK_REC_RANK_EXE_DT PRIMARY KEY (CALC_DATE,CALC_TIME,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)