-- $Id: e355406d0bd7a83df2c32bd553a2df78ddc47e4b $

drop table EDBOARD_ENTEXAM_APPLICANTADDR_DAT
create table EDBOARD_ENTEXAM_APPLICANTADDR_DAT \
( \
    EDBOARD_SCHOOLCD    varchar(12) not null, \
    ENTEXAMYEAR         varchar(4)  not null, \
    APPLICANTDIV        varchar(1)  not null, \
    EXAMNO              varchar(10) not null, \
    FAMILY_REGISTER     varchar(2), \
    ZIPCD               varchar(8), \
    PREF_CD             varchar(2), \
    ADDRESS1            varchar(150), \
    ADDRESS2            varchar(150), \
    TELNO               varchar(14), \
    GNAME               varchar(120), \
    GKANA               varchar(240), \
    GZIPCD              varchar(8), \
    GPREF_CD            varchar(2), \
    GADDRESS1           varchar(150), \
    GADDRESS2           varchar(150), \
    GTELNO              varchar(14), \
    GTELNO2             varchar(14), \
    GFAXNO              varchar(14), \
    RELATIONSHIP        varchar(2), \
    EMERGENCYCALL       varchar(30), \
    EMERGENCYTELNO      varchar(14), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table EDBOARD_ENTEXAM_APPLICANTADDR_DAT add constraint \
PK_ED_ENTEXAM_ADDR primary key (EDBOARD_SCHOOLCD, ENTEXAMYEAR, APPLICANTDIV, EXAMNO)
