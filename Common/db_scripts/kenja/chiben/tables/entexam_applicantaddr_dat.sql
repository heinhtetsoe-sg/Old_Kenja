-- $Id: entexam_applicantaddr_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table entexam_applicantaddr_dat

create table entexam_applicantaddr_dat \
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
    EDBOARDCD           varchar(6),  \
    REGISTERCD          varchar(8),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_applicantaddr_dat add constraint \
pk_entexam_addr primary key (entexamyear,examno)
