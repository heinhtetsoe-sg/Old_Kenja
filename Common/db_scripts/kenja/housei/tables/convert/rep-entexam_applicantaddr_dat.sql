drop table ENTEXAM_APPLICANTADDR_DAT_OLD

create table ENTEXAM_APPLICANTADDR_DAT_OLD like ENTEXAM_APPLICANTADDR_DAT

insert into ENTEXAM_APPLICANTADDR_DAT_OLD select * from ENTEXAM_APPLICANTADDR_DAT

drop table ENTEXAM_APPLICANTADDR_DAT

create table ENTEXAM_APPLICANTADDR_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    EXAMNO              varchar(5)  not null, \
    FAMILY_REGISTER     varchar(2), \
    ZIPCD               varchar(8), \
	PREF_CD				varchar(2), \
    ADDRESS1            varchar(75), \
    ADDRESS2            varchar(75), \
    TELNO               varchar(14), \
    GNAME               varchar(60), \
    GKANA               varchar(120), \
    GZIPCD              varchar(8), \
	GPREF_CD			varchar(2), \
    GADDRESS1           varchar(75), \
    GADDRESS2           varchar(75), \
    GTELNO              varchar(14), \
    GFAXNO              varchar(14), \
    RELATIONSHIP        varchar(2), \
    EMERGENCYCALL       varchar(30), \
    EMERGENCYTELNO      varchar(14), \
    REGISTERCD          varchar(8),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTADDR_DAT add constraint \
PK_ENTEXAM_ADDR primary key (ENTEXAMYEAR, EXAMNO)

insert into ENTEXAM_APPLICANTADDR_DAT \
  select \
    ENTEXAMYEAR, \
    EXAMNO, \
	cast(null as varchar(2)), \
    ZIPCD, \
	cast(null as varchar(2)), \
    ADDRESS1, \
    ADDRESS2, \
    TELNO, \
    GNAME, \
    GKANA, \
    GZIPCD, \
	cast(null as varchar(2)), \
    GADDRESS1, \
    GADDRESS2, \
    GTELNO, \
    GFAXNO, \
    RELATIONSHIP, \
    EMERGENCYCALL, \
    EMERGENCYTELNO, \
    REGISTERCD, \
    UPDATED \
  from ENTEXAM_APPLICANTADDR_DAT_OLD


