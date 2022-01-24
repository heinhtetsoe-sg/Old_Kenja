-- kanji=´Á»ú
-- $Id: entexam_applicantaddr_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_applicantaddr_dat

CREATE TABLE entexam_applicantaddr_dat \
( \
    entexamyear     varchar(4)  not null, \
    testdiv         varchar(1)  not null, \
    examno          varchar(4)  not null, \
    zipcd           varchar(8), \
    address         varchar(153), \
    gzipcd          varchar(8), \
    gaddress        varchar(153), \
    registercd      varchar(8), \
    updated         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_applicantaddr_dat add constraint \
pk_entexam_apadr primary key (entexamyear,testdiv,examno)
