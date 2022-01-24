-- $Id: entexam_interview_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_INTERVIEW_DAT
CREATE TABLE ENTEXAM_INTERVIEW_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(1)    NOT NULL, \
    EXAMNO                    VARCHAR(5)    NOT NULL, \
    INTERVIEW_REMARK          VARCHAR(210), \
    INTERVIEW_VALUE           VARCHAR(1), \
    COMPOSITION_VALUE         VARCHAR(1), \
    INTERVIEW_A               VARCHAR(1), \
    INTERVIEW_B               VARCHAR(1), \
    INTERVIEW_C               VARCHAR(1), \
    REGISTERCD                VARCHAR(8), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_INTERVIEW_DAT ADD CONSTRAINT PK_ENTEXAM_INTERVI PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAMNO)