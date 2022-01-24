-- $Id: entexam_applicant_average_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_APPLICANT_AVERAGE_DAT
CREATE TABLE ENTEXAM_APPLICANT_AVERAGE_DAT( \
    ENTEXAMYEAR             varchar(4)  not null, \
    APPLICANTDIV            varchar(1)  not null, \
    TESTDIV                 varchar(1)  not null, \
    EXAM_TYPE               varchar(1)  not null, \
    SHDIV                   varchar(1)  not null, \
    COURSECD                varchar(1)  not null, \
    MAJORCD                 varchar(3)  not null, \
    EXAMCOURSECD            varchar(4)  not null, \
    TESTSUBCLASSCD          varchar(1)  not null, \
    AVERAGE_MEN             decimal(4,1), \
    AVERAGE_WOMEN           decimal(4,1), \
    AVERAGE_TOTAL           decimal(4,1), \
    MAX_SCORE               smallint, \
    MIN_SCORE               smallint, \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_APPLICANT_AVERAGE_DAT ADD CONSTRAINT PK_ENTEX_APP_AVG PRIMARY KEY (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, SHDIV, COURSECD, MAJORCD, EXAMCOURSECD, TESTSUBCLASSCD)
