-- kanji=´Á»ú
-- $Id: entexam_score_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_score_dat

create table entexam_score_dat \
( \
    entexamyear     varchar(4)  not null, \
    testdiv         varchar(1)  not null, \
    examno          varchar(4)  not null, \
    testsubclasscd  varchar(4)  not null, \
    attend_flg      varchar(1), \
    a_score         smallint, \
    a_std_score     decimal(4,1), \
    a_rank          smallint, \
    b_score         smallint, \
    b_std_score     decimal(4,1), \
    b_rank          smallint, \
    registercd      varchar(8),  \
    updated         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_score_dat add constraint \
pk_entexam_score primary key (entexamyear,testdiv,examno,testsubclasscd)
