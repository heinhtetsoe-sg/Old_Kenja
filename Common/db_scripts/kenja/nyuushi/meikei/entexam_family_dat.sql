-- $Id: 1fb79ec22a5c6a1c888f1583d57b64c4988174a0 $

drop table ENTEXAM_FAMILY_DAT

create table ENTEXAM_FAMILY_DAT( \
    ENTEXAMYEAR     varchar(4)   not null, \
    APPLICANTDIV    varchar(1)   not null, \
    EXAMNO          varchar(10)  not null, \
    RELANO          varchar(2)   not null, \
    RELANAME        varchar(60),  \
    RELAKANA        varchar(120), \
    RELATIONSHIP    varchar(2),   \
    RELA_AGE        varchar(3),   \
    WORKPLACE       varchar(120),   \
    REGISTERCD      varchar(10),  \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_FAMILY_DAT \
add constraint PK_ENTEXAM_FAMI_D \
primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO, RELANO)
