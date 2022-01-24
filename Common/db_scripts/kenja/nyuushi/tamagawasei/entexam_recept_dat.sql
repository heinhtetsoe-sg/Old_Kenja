-- $Id: e9deb69dab9ebb0828d2d8134d7ec5044e870462 $

DROP TABLE ENTEXAM_RECEPT_DAT
CREATE TABLE ENTEXAM_RECEPT_DAT( \
    ENTEXAMYEAR               varchar(4)    not null, \
    APPLICANTDIV              varchar(1)    not null, \
    TESTDIV                   varchar(2)    not null, \
    EXAM_TYPE                 varchar(2)    not null, \
    RECEPTNO                  varchar(10)   not null, \
    EXAMNO                    varchar(10)   not null, \
    ATTEND_ALL_FLG            varchar(1), \
    TOTAL2                    smallint, \
    AVARAGE2                  decimal(4,1), \
    TOTAL_RANK2               smallint, \
    DIV_RANK2                 smallint, \
    TOTAL4                    smallint, \
    AVARAGE4                  decimal(4,1), \
    TOTAL_RANK4               smallint, \
    DIV_RANK4                 smallint, \
    TOTAL1                    smallint, \
    AVARAGE1                  decimal(4,1), \
    TOTAL_RANK1               smallint, \
    DIV_RANK1                 smallint, \
    TOTAL3                    smallint, \
    AVARAGE3                  decimal(4,1), \
    TOTAL_RANK3               smallint, \
    DIV_RANK3                 smallint, \
    JUDGE_DEVIATION           decimal(4,1), \
    JUDGE_DEVIATION_DIV       varchar(1), \
    JUDGE_DEVIATION_RANK      smallint, \
    LINK_JUDGE_DEVIATION      decimal(4,1), \
    LINK_JUDGE_DEVIATION_DIV  varchar(1), \
    LINK_JUDGE_DEVIATION_RANK smallint, \
    JUDGE_EXAM_TYPE           varchar(1), \
    JUDGEDIV                  varchar(1), \
    HONORDIV                  varchar(1), \
    ADJOURNMENTDIV            varchar(1), \
    JUDGELINE                 varchar(1), \
    PROCEDUREDIV1             varchar(1), \
    PROCEDUREDATE1            date, \
    DISTINCT_ID               varchar(3), \
    TEST_NAME_ABBV            varchar(100), \
    REGISTERCD                varchar(10), \
    UPDATED                   timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_RECEPT_DAT ADD CONSTRAINT PK_ENTEXAM_RCPT PRIMARY KEY (ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,RECEPTNO)