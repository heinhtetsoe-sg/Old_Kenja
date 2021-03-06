-- $Id$

DROP TABLE MOCK_TRAINREMARK_DAT

CREATE TABLE MOCK_TRAINREMARK_DAT( \
    SCHREGNO            VARCHAR(8) NOT NULL, \
    GRADE1_REMARK       VARCHAR(150), \
    GRADE2_REMARK       VARCHAR(150), \
    GRADE3_REMARK       VARCHAR(150), \
    CONDITION           VARCHAR(500), \
    HOPE_COLLEGE_NAME1  VARCHAR(150), \
    HOPE_COURSE_NAME1   VARCHAR(150), \
    HOPE_COLLEGE_NAME2  VARCHAR(150), \
    HOPE_COURSE_NAME2   VARCHAR(150), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MOCK_TRAINREMARK_DAT ADD CONSTRAINT PK_MOCK_TRAIN_D PRIMARY KEY (SCHREGNO)
