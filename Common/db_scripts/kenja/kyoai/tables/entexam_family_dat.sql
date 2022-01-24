-- $Id: entexam_family_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE ENTEXAM_FAMILY_DAT
CREATE TABLE ENTEXAM_FAMILY_DAT( \
    ENTEXAMYEAR             varchar(4)  not null, \
    EXAMNO                  varchar(5)  not null, \
    SEQ                     integer     not null, \
    NAME                    varchar(60), \
    NAME_KANA               varchar(120), \
    SEX                     varchar(1), \
    ERACD                   varchar(1), \
    BIRTH_Y                 varchar(2), \
    BIRTH_M                 varchar(2), \
    BIRTH_D                 varchar(2), \
    BIRTHDAY                date, \
    AGE                     smallint, \
    RELATIONSHIP            varchar(2), \
    REMARK                  varchar(60), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ENTEXAM_FAMILY_DAT ADD CONSTRAINT PK_EXAM_FAMILY PRIMARY KEY (ENTEXAMYEAR, EXAMNO, SEQ)
