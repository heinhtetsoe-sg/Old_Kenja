-- $Id: 2ffb4f8eace0387bfca78f77d59d5283c1135581 $

drop table EDBOARD_ENTEXAM_APPLICANTADDR_DAT_OLD
create table EDBOARD_ENTEXAM_APPLICANTADDR_DAT_OLD like EDBOARD_ENTEXAM_APPLICANTADDR_DAT
insert into EDBOARD_ENTEXAM_APPLICANTADDR_DAT_OLD select * from EDBOARD_ENTEXAM_APPLICANTADDR_DAT

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

insert into EDBOARD_ENTEXAM_APPLICANTADDR_DAT \
select \
    EDBOARD_SCHOOLCD, \
    ENTEXAMYEAR, \
    APPLICANTDIV, \
    EXAMNO, \
    FAMILY_REGISTER, \
    ZIPCD, \
    PREF_CD, \
    ADDRESS1, \
    ADDRESS2, \
    TELNO, \
    GNAME, \
    GKANA, \
    GZIPCD, \
    GPREF_CD, \
    GADDRESS1, \
    GADDRESS2, \
    GTELNO, \
    GTELNO2, \
    GFAXNO, \
    RELATIONSHIP, \
    EMERGENCYCALL, \
    EMERGENCYTELNO, \
    REGISTERCD, \
    UPDATED \
from \
    EDBOARD_ENTEXAM_APPLICANTADDR_DAT_OLD
