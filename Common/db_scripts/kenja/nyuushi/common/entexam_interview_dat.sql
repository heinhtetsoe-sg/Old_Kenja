-- $Id: 19fbe1b69d69f29364dd7ea2debaa215eb3c8bda $

DROP TABLE ENTEXAM_INTERVIEW_DAT
CREATE TABLE ENTEXAM_INTERVIEW_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(2)    NOT NULL, \
    EXAMNO                    VARCHAR(10)   NOT NULL, \
    INTERVIEW_REMARK          VARCHAR(300), \
    INTERVIEW_VALUE           VARCHAR(2), \
    COMPOSITION_VALUE         VARCHAR(1), \
    INTERVIEW_A               VARCHAR(1), \
    INTERVIEW_B               VARCHAR(1), \
    INTERVIEW_C               VARCHAR(1), \
    INTERVIEW_REMARK2         VARCHAR(300), \
    INTERVIEW_VALUE2          VARCHAR(3), \
    INTERVIEW_REMARK3         VARCHAR(300), \
    ATTEND_FLG                VARCHAR(1), \
    SCORE                     SMALLINT, \
    STD_SCORE                 DECIMAL(4,1), \
    SCORE1                    SMALLINT, \
    SCORE2                    SMALLINT, \
    OTHER_REMARK1             VARCHAR(150), \
    OTHER_REMARK2             VARCHAR(150), \
    OTHER_REMARK3             VARCHAR(150), \
    OTHER_REMARK4             VARCHAR(150), \
    OTHER_REMARK5             VARCHAR(150), \
    OTHER_REMARK6             VARCHAR(150), \
    OTHER_REMARK7             VARCHAR(150), \
    OTHER_REMARK8             VARCHAR(150), \
    OTHER_REMARK9             VARCHAR(150), \
    OTHER_REMARK10            VARCHAR(150), \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_INTERVIEW_DAT ADD CONSTRAINT PK_ENTEXAM_INTERVI PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAMNO)