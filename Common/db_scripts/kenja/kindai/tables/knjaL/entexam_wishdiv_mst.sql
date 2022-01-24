-- kanji=´Á»ú
-- $Id: entexam_wishdiv_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_wishdiv_mst

create table entexam_wishdiv_mst \
( \
    entexamyear     varchar(4)  not null, \
    testdiv         varchar(1)  not null, \
    desirediv       varchar(2)  not null, \
    wishno          varchar(1)  not null, \
    coursecd        varchar(1), \
    majorcd         varchar(3), \
    examcoursecd    varchar(4), \
    registercd      varchar(8),  \
    updated         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_wishdiv_mst add constraint \
pk_entexam_wish primary key (entexamyear,testdiv,desirediv,wishno)
