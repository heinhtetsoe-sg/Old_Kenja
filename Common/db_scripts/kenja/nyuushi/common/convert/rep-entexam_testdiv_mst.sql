-- $Id: ff47cfb5a65a79b27c62ed15d0f16a88dba24592 $

drop table ENTEXAM_TESTDIV_MST_OLD
create table ENTEXAM_TESTDIV_MST_OLD like ENTEXAM_TESTDIV_MST
insert into ENTEXAM_TESTDIV_MST_OLD select * from ENTEXAM_TESTDIV_MST

DROP TABLE ENTEXAM_TESTDIV_MST
CREATE TABLE ENTEXAM_TESTDIV_MST( \
    ENTEXAMYEAR         VARCHAR(4)  NOT NULL, \
    APPLICANTDIV        VARCHAR(1)  NOT NULL, \
    TESTDIV             VARCHAR(2)  NOT NULL, \
    TESTDIV_NAME        VARCHAR(60), \
    TESTDIV_ABBV        VARCHAR(30), \
    INTERVIEW_DIV       VARCHAR(1), \
    CAPACITY            SMALLINT, \
    TEST_DATE           DATE, \
    TEST_DATE1          VARCHAR(30), \
    TEST_DATE2          VARCHAR(30), \
    PROCEDURE_DATE1     VARCHAR(30), \
    PROCEDURE_DATE2     VARCHAR(30), \
    DEFAULT_FLG         VARCHAR(1), \
    KIKOKU_FLG          VARCHAR(1), \
    PRETEST_FLG         VARCHAR(1), \
    SHOWORDER           SMALLINT, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_TESTDIV_MST ADD CONSTRAINT PK_ENTEXAM_TESTDIV PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV)

INSERT INTO ENTEXAM_TESTDIV_MST \
    SELECT \
        ENTEXAMYEAR, \
        APPLICANTDIV, \
        TESTDIV, \
        TESTDIV_NAME, \
        TESTDIV_ABBV, \
        cast(null as varchar(1)) AS INTERVIEW_DIV, \
        cast(null as smallint) AS CAPACITY, \
        cast(null as date) AS TEST_DATE, \
        TEST_DATE1, \
        TEST_DATE2, \
        PROCEDURE_DATE1, \
        PROCEDURE_DATE2, \
        DEFAULT_FLG, \
        KIKOKU_FLG, \
        PRETEST_FLG, \
        SHOWORDER, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ENTEXAM_TESTDIV_MST_OLD