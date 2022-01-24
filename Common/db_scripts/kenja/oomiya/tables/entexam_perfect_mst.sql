drop table entexam_perfect_mst

create table entexam_perfect_mst \
( \
    entexamyear         varchar(4)  not null, \
    testdiv             varchar(1)  not null, \
    coursecd            varchar(1)  not null, \
    majorcd             varchar(3)  not null, \
    examcoursecd        varchar(4)  not null, \
    testsubclasscd      varchar(1)  not null, \
    perfect             smallint, \
    registercd          varchar(8),  \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_perfect_mst add constraint \
pk_entexam_perf primary key (entexamyear,testdiv,coursecd,majorcd,examcoursecd,testsubclasscd)
