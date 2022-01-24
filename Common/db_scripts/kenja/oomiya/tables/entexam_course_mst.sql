drop table entexam_course_mst

create table entexam_course_mst \
( \
    entexamyear     varchar(4)  not null, \
    coursecd        varchar(1)  not null, \
    majorcd         varchar(3)  not null, \
    examcoursecd    varchar(4)  not null, \
    examcourse_name varchar(45), \
    examcourse_mark varchar(1), \
    capacity        smallint, \
    registercd      varchar(8),  \
    updated         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_course_mst add constraint \
pk_entexam_course primary key (entexamyear,coursecd,majorcd,examcoursecd)
