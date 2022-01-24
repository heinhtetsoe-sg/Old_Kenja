-- $Id: entexam_recept_detail_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_RECEPT_DETAIL_DAT
CREATE TABLE ENTEXAM_RECEPT_DETAIL_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(1)    NOT NULL, \
    EXAM_TYPE                 VARCHAR(1)    NOT NULL, \
    RECEPTNO                  VARCHAR(5)    NOT NULL, \
    SEQ                       varchar(3)    not null, \
    REMARK1                   varchar(150), \
    REMARK2                   varchar(150), \
    REMARK3                   varchar(150), \
    REMARK4                   varchar(150), \
    REMARK5                   varchar(150), \
    REMARK6                   varchar(150), \
    REMARK7                   varchar(150), \
    REMARK8                   varchar(150), \
    REMARK9                   varchar(150), \
    REMARK10                  varchar(150), \
    REGISTERCD                VARCHAR(8), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_RECEPT_DETAIL_DAT ADD CONSTRAINT PK_ENTEXAM_RCPTD PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO, SEQ)