-- kanji=´Á»ú
-- $Id: entexam_perfect_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_perfect_mst

create table entexam_perfect_mst \
( \
    entexamyear         varchar(4)  not null, \
    testdiv             varchar(1)  not null, \
    coursecd            varchar(1)  not null, \
    majorcd             varchar(3)  not null, \
    examcoursecd        varchar(4)  not null, \
    testsubclasscd      varchar(4)  not null, \
    perfect             smallint, \
    autocalc            varchar(1), \
    inc_magnification   decimal(2,1), \
    adoptiondiv         varchar(1), \
    registercd          varchar(8),  \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_perfect_mst add constraint \
pk_entexam_perf primary key (entexamyear,testdiv,coursecd,majorcd,examcoursecd,testsubclasscd)
