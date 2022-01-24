-- kanji=漢字
-- $Id: 3000206474a5c6266f6ced4ae95088479115ba5e $

DROP TABLE ENTEXAM_RECEPT_DETAIL_DAT

CREATE TABLE ENTEXAM_RECEPT_DETAIL_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(1)    NOT NULL, \
    EXAM_TYPE                 VARCHAR(1)    NOT NULL, \
    RECEPTNO                  VARCHAR(10)   NOT NULL, \
    SEQ                       VARCHAR(3)    NOT NULL, \
    REMARK1                   VARCHAR(150), \
    REMARK2                   VARCHAR(150), \
    REMARK3                   VARCHAR(150), \
    REMARK4                   VARCHAR(150), \
    REMARK5                   VARCHAR(150), \
    REMARK6                   VARCHAR(150), \
    REMARK7                   VARCHAR(150), \
    REMARK8                   VARCHAR(150), \
    REMARK9                   VARCHAR(150), \
    REMARK10                  VARCHAR(150), \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_RECEPT_DETAIL_DAT ADD CONSTRAINT PK_ENTEXAM_RCPTD PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO, SEQ)