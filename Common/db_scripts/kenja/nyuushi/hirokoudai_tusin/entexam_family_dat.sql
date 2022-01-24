-- $Id: 604ce4ad77e6744d4b7ce5a12c67c6d43ea237d4 $

drop table ENTEXAM_FAMILY_DAT

create table ENTEXAM_FAMILY_DAT( \
    ENTEXAMYEAR     varchar(4)   not null, \
    EXAMNO          varchar(10)  not null, \
    RELANO          varchar(2)   not null, \
    RELANAME        varchar(60),  \
    RELAKANA        varchar(120), \
    RELATIONSHIP    varchar(2),   \
    RELA_AGE        varchar(3),   \
    REGISTERCD      varchar(10),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_FAMILY_DAT \
add constraint PK_ENTEXAM_FAMI_D \
primary key (ENTEXAMYEAR, EXAMNO, RELANO)
