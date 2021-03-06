-- $Id: 1595f83e2af64b00254f1db4132abd3bff7057d5 $


DROP TABLE ENTEXAM_DOCUMENT_VIEW_DAT
CREATE TABLE ENTEXAM_DOCUMENT_VIEW_DAT( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    TESTDIV        VARCHAR(2)    NOT NULL, \
    EXAM_TYPE      VARCHAR(2)    NOT NULL, \
    EXAMNO         VARCHAR(10)   NOT NULL, \
    SEQ            VARCHAR(3)    NOT NULL, \
    REMARK1        VARCHAR(10) , \
    REMARK2        VARCHAR(10) , \
    REMARK3        VARCHAR(10) , \
    REMARK4        VARCHAR(10) , \
    REMARK5        VARCHAR(10) , \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_DOCUMENT_VIEW_DAT ADD CONSTRAINT PK_ENTEXAM_DOCUMENT_VIEW PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, EXAMNO, SEQ)

