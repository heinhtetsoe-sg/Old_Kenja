-- $Id: 73bba46134e9bc19d328996d36ac3339ed0633fc $

DROP TABLE ENTEXAM_RECEPT_DAT
CREATE TABLE ENTEXAM_RECEPT_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(2)    NOT NULL, \
    EXAM_TYPE                 VARCHAR(2)    NOT NULL, \
    RECEPTNO                  VARCHAR(10)   NOT NULL, \
    EXAMNO                    VARCHAR(10)   NOT NULL, \
    ATTEND_ALL_FLG            VARCHAR(1), \
    TOTAL2                    SMALLINT, \
    AVARAGE2                  DECIMAL(4,1), \
    TOTAL_RANK2               SMALLINT, \
    DIV_RANK2                 SMALLINT, \
    TOTAL4                    SMALLINT, \
    AVARAGE4                  DECIMAL(4,1), \
    TOTAL_RANK4               SMALLINT, \
    DIV_RANK4                 SMALLINT, \
    TOTAL1                    SMALLINT, \
    AVARAGE1                  DECIMAL(4,1), \
    TOTAL_RANK1               SMALLINT, \
    DIV_RANK1                 SMALLINT, \
    TOTAL3                    SMALLINT, \
    AVARAGE3                  DECIMAL(4,1), \
    TOTAL_RANK3               SMALLINT, \
    DIV_RANK3                 SMALLINT, \
    JUDGE_DEVIATION           DECIMAL(4,1), \
    JUDGE_DEVIATION_DIV       VARCHAR(1), \
    JUDGE_DEVIATION_RANK      SMALLINT, \
    LINK_JUDGE_DEVIATION      DECIMAL(4,1), \
    LINK_JUDGE_DEVIATION_DIV  VARCHAR(1), \
    LINK_JUDGE_DEVIATION_RANK SMALLINT, \
    JUDGE_EXAM_TYPE           VARCHAR(2), \
    JUDGEDIV                  VARCHAR(1), \
    HONORDIV                  VARCHAR(1), \
    ADJOURNMENTDIV            VARCHAR(1), \
    JUDGELINE                 varchar(1), \
    PROCEDUREDIV1             VARCHAR(1), \
    PROCEDUREDATE1            DATE, \
    DISTINCT_ID               varchar(3), \
    TEST_NAME_ABBV            varchar(100), \
    SEX_RANK4                 SMALLINT, \
    SEX_RANK1                 SMALLINT, \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_RECEPT_DAT ADD CONSTRAINT PK_ENTEXAM_RCPT PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO)