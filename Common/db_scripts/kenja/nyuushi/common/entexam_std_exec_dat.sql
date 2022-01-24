-- $Id: $

DROP TABLE ENTEXAM_STD_EXEC_DAT
CREATE TABLE ENTEXAM_STD_EXEC_DAT( \
    EXEC_DATE           DATE          NOT NULL, \
    EXEC_TIME           TIME          NOT NULL, \
    YEAR                VARCHAR(4)    , \
    EXAM_SCHOOL_KIND    VARCHAR(2)    , \
    APPLICANT_DIV       VARCHAR(2)    , \
    COURSE_DIV          VARCHAR(4)    , \
    FREQUENCY           VARCHAR(2)    , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_EXEC_DAT ADD CONSTRAINT PK_ENTEXAM_STD_EXEC_DAT PRIMARY KEY (EXEC_DATE, EXEC_TIME)
