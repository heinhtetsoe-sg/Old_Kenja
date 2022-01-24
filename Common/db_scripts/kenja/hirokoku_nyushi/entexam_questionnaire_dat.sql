-- $Id: entexam_questionnaire_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_QUESTIONNAIRE_DAT
CREATE TABLE ENTEXAM_QUESTIONNAIRE_DAT( \
    ENTEXAMYEAR             VARCHAR(4)    NOT NULL, \
    APPLICANTDIV            VARCHAR(1)    NOT NULL, \
    TESTDIV                 VARCHAR(1)    NOT NULL, \
    EXAMNO                  VARCHAR(5)    NOT NULL, \
    SH_FLG                  VARCHAR(1), \
    SH_SCHOOLNAME1          VARCHAR(75), \
    SH_SCHOOLNAME2          VARCHAR(75), \
    SH_SCHOOLNAME3          VARCHAR(75), \
    SH_SCHOOLNAME4          VARCHAR(75), \
    SH_SCHOOLNAME5          VARCHAR(75), \
    SH_SCHOOLNAME6          VARCHAR(75), \
    SH_JUDGEMENT1           VARCHAR(1), \
    SH_JUDGEMENT2           VARCHAR(1), \
    SH_JUDGEMENT3           VARCHAR(1), \
    SH_JUDGEMENT4           VARCHAR(1), \
    SH_JUDGEMENT5           VARCHAR(1), \
    SH_JUDGEMENT6           VARCHAR(1), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_QUESTIONNAIRE_DAT ADD CONSTRAINT PK_ENTEXAM_QUESTIO PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAMNO)