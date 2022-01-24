-- $Id: entexam_passingmark_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table entexam_passingmark_mst

create table entexam_passingmark_mst \
( \
    entexamyear         varchar(4)  not null, \
    applicantdiv        varchar(1)  not null, \
    testdiv             varchar(1)  not null, \
    exam_type           varchar(1)  not null, \
    shdiv               varchar(1)  not null, \
    coursecd            varchar(1)  not null, \
    majorcd             varchar(3)  not null, \
    examcoursecd        varchar(4)  not null, \
    border_score        smallint, \
    success_cnt         smallint, \
    back_rate           smallint, \
    capa_cnt            smallint, \
    registercd          varchar(8), \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_passingmark_mst add constraint \
pk_entexam_pass primary key (entexamyear,applicantdiv,testdiv,exam_type,shdiv,coursecd,majorcd,examcoursecd)
