-- $Id: entexam_perfect_examtype_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_PERFECT_EXAMTYPE_MST
CREATE TABLE ENTEXAM_PERFECT_EXAMTYPE_MST( \
    ENTEXAMYEAR    VARCHAR(4)    NOT NULL, \
    APPLICANTDIV   VARCHAR(1)    NOT NULL, \
    TESTDIV        VARCHAR(1)    NOT NULL, \
    COURSECD       VARCHAR(1)    NOT NULL, \
    MAJORCD        VARCHAR(3)    NOT NULL, \
    EXAMCOURSECD   VARCHAR(4)    NOT NULL, \
    TESTSUBCLASSCD VARCHAR(1)    NOT NULL, \
    EXAM_TYPE      VARCHAR(1)    NOT NULL, \
    PERFECT        SMALLINT, \
    RATE           SMALLINT, \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_PERFECT_EXAMTYPE_MST ADD CONSTRAINT PK_ENTEXAM_PERF_EX PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,COURSECD,MAJORCD,EXAMCOURSECD,TESTSUBCLASSCD,EXAM_TYPE)