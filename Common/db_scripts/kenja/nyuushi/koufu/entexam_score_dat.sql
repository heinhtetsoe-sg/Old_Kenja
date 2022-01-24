-- $Id: 30aa4686e0779cb3bc0eb2125d8b8f48348e645f $

DROP TABLE ENTEXAM_SCORE_DAT
CREATE TABLE ENTEXAM_SCORE_DAT( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    TESTDIV        VARCHAR(1)    NOT NULL, \
    EXAM_TYPE      VARCHAR(1)    NOT NULL, \
    RECEPTNO       VARCHAR(5)    NOT NULL, \
    TESTSUBCLASSCD VARCHAR(1)    NOT NULL, \
    ATTEND_FLG     VARCHAR(1), \
    SCORE          SMALLINT, \
    STD_SCORE      DECIMAL(5,2), \
    RANK           SMALLINT, \
    SCORE2         SMALLINT, \
    SCORE3         SMALLINT, \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_SCORE_DAT ADD CONSTRAINT PK_ENTEXAM_SCORE PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO,TESTSUBCLASSCD)