-- $Id: entexam_hall_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table entexam_hall_dat

create table entexam_hall_dat \
( \
    testdiv             varchar(1)  not null, \
    exam_type           varchar(1)  not null, \
    examhallcd          varchar(4)  not null, \
    examhall_name       varchar(30), \
    capa_cnt            smallint, \
    s_receptno          varchar(4), \
    e_receptno          varchar(4), \
    registercd          varchar(8), \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_hall_dat add constraint \
pk_entexam_hall primary key (testdiv,exam_type,examhallcd)
