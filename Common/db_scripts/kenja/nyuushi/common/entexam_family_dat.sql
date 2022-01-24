-- $Id: bda3d6863669a39c2febb59bd992daeb0a8137dc $

drop table ENTEXAM_FAMILY_DAT

create table ENTEXAM_FAMILY_DAT( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    EXAMNO          varchar(10)  not null, \
    SEQ             integer     not null, \
    NAME            varchar(60), \
    NAME_KANA       varchar(120), \
    SEX             varchar(1), \
    ERACD           varchar(1), \
    BIRTH_Y         varchar(2), \
    BIRTH_M         varchar(2), \
    BIRTH_D         varchar(2), \
    BIRTHDAY        date, \
    AGE             smallint, \
    RELATIONSHIP    varchar(2), \
    WORKPLACE       varchar(120), \
    REMARK          varchar(60), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_FAMILY_DAT \
add constraint PK_ENTEXAM_FAMI_D \
primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO, SEQ)
