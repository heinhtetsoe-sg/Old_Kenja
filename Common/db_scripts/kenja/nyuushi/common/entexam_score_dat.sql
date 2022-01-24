-- $Id: c3680ae2f81b8c9391d3c567b09a5faddc60c4a0 $

DROP TABLE ENTEXAM_SCORE_DAT
CREATE TABLE ENTEXAM_SCORE_DAT( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    TESTDIV        VARCHAR(2)    NOT NULL, \
    EXAM_TYPE      VARCHAR(2)    NOT NULL, \
    RECEPTNO       VARCHAR(10)   NOT NULL, \
    TESTSUBCLASSCD VARCHAR(2)    NOT NULL, \
    ATTEND_FLG     VARCHAR(1), \
    SCORE          SMALLINT, \
    STD_SCORE      DECIMAL(5,2), \
    RANK           SMALLINT, \
    SCORE2         SMALLINT, \
    SCORE3         SMALLINT, \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    SEX_RANK       SMALLINT \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_SCORE_DAT ADD CONSTRAINT PK_ENTEXAM_SCORE PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO,TESTSUBCLASSCD)
