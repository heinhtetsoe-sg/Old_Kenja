-- $Id: c414bed4078808be00a367d59ae13d077f4a63f0 $

DROP TABLE MOCK_PERFECT_COURSE_DAT
CREATE TABLE MOCK_PERFECT_COURSE_DAT( \
    YEAR             VARCHAR(4)    NOT NULL, \
    MOCKCD           VARCHAR(9)    NOT NULL, \
    COURSE_DIV       VARCHAR(1)    NOT NULL, \
    MOCK_SUBCLASS_CD VARCHAR(6)    NOT NULL, \
    DIV              VARCHAR(2)    NOT NULL, \
    GRADE            VARCHAR(2)    NOT NULL, \
    COURSECD         VARCHAR(1)    NOT NULL, \
    MAJORCD          VARCHAR(3)    NOT NULL, \
    COURSECODE       VARCHAR(4)    NOT NULL, \
    PERFECT          SMALLINT, \
    PASS_SCORE       SMALLINT, \
    REGISTERCD       VARCHAR(8), \
    UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MOCK_PERFECT_COURSE_DAT ADD CONSTRAINT PK_MOCK_PER_C_DAT PRIMARY KEY (YEAR,MOCKCD,COURSE_DIV,MOCK_SUBCLASS_CD,DIV,GRADE,COURSECD,MAJORCD,COURSECODE)