-- $Id: $

DROP TABLE ENTEXAM_STD_APPLICANT_MST
CREATE TABLE ENTEXAM_STD_APPLICANT_MST( \
    YEAR                VARCHAR(4)    NOT NULL, \
    EXAM_SCHOOL_KIND    VARCHAR(2)    NOT NULL, \
    APPLICANT_DIV       VARCHAR(2)    NOT NULL, \
    APPLICANT_NAME      VARCHAR(30)   , \
    REGISTERCD          VARCHAR(10)   , \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
ALTER TABLE ENTEXAM_STD_APPLICANT_MST ADD CONSTRAINT PK_ENTEXAM_STD_APPLICANT_MST PRIMARY KEY (YEAR, EXAM_SCHOOL_KIND, APPLICANT_DIV)