drop table entexam_wishdiv_mst

create table entexam_wishdiv_mst \
( \
    entexamyear     varchar(4)  not null, \
    desirediv       varchar(1)  not null, \
    wishno          varchar(1)  not null, \
    coursecd        varchar(1), \
    majorcd         varchar(3), \
    examcoursecd    varchar(4), \
    registercd      varchar(8),  \
    updated         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_wishdiv_mst add constraint \
pk_entexam_wish primary key (entexamyear,desirediv,wishno)


