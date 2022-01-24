-- $Id: 07e6beb6bb14e78cbc0560114696fa2e28f5f547 $
drop table ENTEXAM_APPLICANTADDR_DAT

create table ENTEXAM_APPLICANTADDR_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    EXAMNO              varchar(5)  not null, \
    FAMILY_REGISTER     varchar(2), \
    ZIPCD               varchar(8), \
    PREF_CD             varchar(2), \
    ADDRESS1            varchar(150), \
    ADDRESS2            varchar(150), \
    TELNO               varchar(14), \
    GNAME               varchar(60), \
    GKANA               varchar(120), \
    GZIPCD              varchar(8), \
    GPREF_CD            varchar(2), \
    GADDRESS1           varchar(150), \
    GADDRESS2           varchar(150), \
    GTELNO              varchar(14), \
    GFAXNO              varchar(14), \
    RELATIONSHIP        varchar(2), \
    EMERGENCYCALL       varchar(30), \
    EMERGENCYTELNO      varchar(14), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTADDR_DAT add constraint \
PK_ENTEXAM_ADDR primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)
