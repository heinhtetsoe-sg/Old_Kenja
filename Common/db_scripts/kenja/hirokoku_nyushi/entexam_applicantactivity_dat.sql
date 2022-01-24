-- $Id: entexam_applicantactivity_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_APPLICANTACTIVITY_DAT
CREATE TABLE ENTEXAM_APPLICANTACTIVITY_DAT( \
    ENTEXAMYEAR         VARCHAR(4)    NOT NULL, \
    EXAMNO              VARCHAR(5)    NOT NULL, \
    ACTIVITY            VARCHAR(60), \
    RESULTS             VARCHAR(240), \
    SECTION             VARCHAR(60), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_APPLICANTACTIVITY_DAT ADD CONSTRAINT PK_ENTEXAM_APACTIV PRIMARY KEY (ENTEXAMYEAR,EXAMNO)