-- $Id: entexam_judge_tmp.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_JUDGE_TMP
CREATE TABLE ENTEXAM_JUDGE_TMP( \
    ENTEXAMYEAR     VARCHAR(4)    NOT NULL, \
    APPLICANTDIV    VARCHAR(1)    NOT NULL, \
    TESTDIV         VARCHAR(1)    NOT NULL, \
    EXAM_TYPE       VARCHAR(1)    NOT NULL, \
    RECEPTNO        VARCHAR(5)    NOT NULL, \
    EXAMNO          VARCHAR(5)    NOT NULL, \
    JUDGE_EXAM_TYPE VARCHAR(1), \
    JUDGEDIV        VARCHAR(1), \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_JUDGE_TMP ADD CONSTRAINT PK_ENTEXAM_JUDGE PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO)