-- $Id: rep-entexam_applicantaddr_dat.sql 64344 2019-01-08 04:54:01Z tawada $
drop table entexam_applicantaddr_dat_old
create table entexam_applicantaddr_dat_old like entexam_applicantaddr_dat
insert into entexam_applicantaddr_dat_old select * from entexam_applicantaddr_dat

create table entexam_applicantaddr_dat \
( \
    entexamyear         varchar(4)  not null, \
    examno              varchar(5)  not null, \
    zipcd               varchar(8), \
    address1            varchar(150), \
    address2            varchar(150), \
    telno               varchar(14), \
    gname               varchar(60), \
    gkana               varchar(120), \
    gzipcd              varchar(8), \
    gaddress1           varchar(150), \
    gaddress2           varchar(150), \
    gtelno              varchar(14), \
    registercd          varchar(10),  \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_applicantaddr_dat add constraint \
pk_entexam_addr primary key (entexamyear,examno)

insert into entexam_applicantaddr_dat \
( \
    entexamyear , \
    examno      , \
    zipcd       , \
    address1    , \
    address2    , \
    telno       , \
    gname       , \
    gkana       , \
    gzipcd      , \
    gaddress1   , \
    gaddress2   , \
    gtelno      , \
    registercd  , \
    updated ) \
select \
    entexamyear , \
    examno      , \
    zipcd       , \
    address1    , \
    address2    , \
    telno       , \
    gname       , \
    gkana       , \
    gzipcd      , \
    gaddress1   , \
    gaddress2   , \
    gtelno      , \
    registercd  ,  \
    updated \
from entexam_applicantaddr_dat_old
