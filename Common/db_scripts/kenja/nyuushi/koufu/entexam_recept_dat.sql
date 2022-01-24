-- $Id: 5d4d0dd6174bc4003f336fee24798ec5acc980d9 $

DROP TABLE ENTEXAM_RECEPT_DAT
CREATE TABLE ENTEXAM_RECEPT_DAT( \
    ENTEXAMYEAR               VARCHAR(4)    NOT NULL, \
    APPLICANTDIV              VARCHAR(1)    NOT NULL, \
    TESTDIV                   VARCHAR(1)    NOT NULL, \
    EXAM_TYPE                 VARCHAR(1)    NOT NULL, \
    RECEPTNO                  VARCHAR(5)    NOT NULL, \
    EXAMNO                    VARCHAR(5)    NOT NULL, \
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
    JUDGE_EXAM_TYPE           VARCHAR(1), \
    JUDGEDIV                  VARCHAR(1), \
    HONORDIV                  VARCHAR(1), \
    ADJOURNMENTDIV            VARCHAR(1), \
    JUDGELINE                 varchar(1), \
    PROCEDUREDIV1             VARCHAR(1), \
    PROCEDUREDATE1            DATE, \
    REGISTERCD                VARCHAR(10), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_RECEPT_DAT ADD CONSTRAINT PK_ENTEXAM_RCPT PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO)