-- $Id: 37c911ca40d7ed339711e2e45e7864377b1b138e $

DROP TABLE EDBOARD_ENTEXAM_SCORE_DAT
CREATE TABLE EDBOARD_ENTEXAM_SCORE_DAT( \
    EDBOARD_SCHOOLCD    VARCHAR(12)     NOT NULL, \
    ENTEXAMYEAR         VARCHAR(4)      NOT NULL, \
    APPLICANTDIV        VARCHAR(1)      NOT NULL, \
    TESTDIV             VARCHAR(2)      NOT NULL, \
    EXAM_TYPE           VARCHAR(2)      NOT NULL, \
    RECEPTNO            VARCHAR(10)     NOT NULL, \
    TESTSUBCLASSCD      VARCHAR(1)      NOT NULL, \
    ATTEND_FLG          VARCHAR(1), \
    SCORE               SMALLINT, \
    STD_SCORE           DECIMAL(5,2), \
    RANK                SMALLINT, \
    SCORE2              SMALLINT, \
    SCORE3              SMALLINT, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EDBOARD_ENTEXAM_SCORE_DAT ADD CONSTRAINT \
PK_ED_EEXAM_SCORE PRIMARY KEY (EDBOARD_SCHOOLCD, ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO, TESTSUBCLASSCD)
