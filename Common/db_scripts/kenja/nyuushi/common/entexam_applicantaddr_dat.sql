-- $Id: dfc54398907099ea4bd666a9aab8cbe94e7ddf94 $
drop table ENTEXAM_APPLICANTADDR_DAT

create table ENTEXAM_APPLICANTADDR_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    EXAMNO              varchar(10) not null, \
    FAMILY_REGISTER     varchar(2), \
    ZIPCD               varchar(8), \
    PREF_CD             varchar(2), \
    ADDRESS1            varchar(300), \
    ADDRESS2            varchar(300), \
    TELNO               varchar(14), \
    EMAIL               varchar(50), \
    GNAME               varchar(120), \
    GKANA               varchar(240), \
    GZIPCD              varchar(8), \
    GPREF_CD            varchar(2), \
    GADDRESS1           varchar(300), \
    GADDRESS2           varchar(300), \
    GTELNO              varchar(14), \
    GEMAIL              varchar(50), \
    GTELNO2             varchar(14), \
    GFAXNO              varchar(14), \
    GRELATIONSHIP       varchar(2), \
    GJOB                varchar(150), \
    SEND_NAME           varchar(120), \
    SEND_KANA           varchar(240), \
    SEND_ZIPCD          varchar(8), \
    SEND_PREF_CD        varchar(2), \
    SEND_ADDRESS1       varchar(300), \
    SEND_ADDRESS2       varchar(300), \
    SEND_TELNO          varchar(14), \
    SEND_EMAIL          varchar(50), \
    SEND_TELNO2         varchar(14), \
    SEND_FAXNO          varchar(14), \
    SEND_RELATIONSHIP   varchar(2), \
    SEND_JOB            varchar(150), \
    EMERGENCYCALL       varchar(150), \
    EMERGENCYTELNO      varchar(14), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTADDR_DAT add constraint \
PK_ENTEXAM_ADDR primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)
