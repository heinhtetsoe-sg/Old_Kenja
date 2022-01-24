-- kanji=´Á»ú
-- $Id: entexam_judgecomp_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_judgecomp_mst

create table entexam_judgecomp_mst \
( \
    entexamyear         varchar(4)  not null, \
    coursecd            varchar(1)  not null, \
    majorcd             varchar(3)  not null, \
    examcoursecd        varchar(4)  not null, \
    shdiv               varchar(1)  not null, \
    judgement           varchar(1)  not null, \
    cmp_coursecd        varchar(1)  not null, \
    cmp_majorcd         varchar(3)  not null, \
    cmp_examcoursecd    varchar(4)  not null, \
    s_judgement         varchar(1), \
    h_judgement         varchar(1), \
    registercd          varchar(8),  \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_judgecomp_mst add constraint \
pk_entexam_jcmp primary key (entexamyear,coursecd,majorcd,examcoursecd,shdiv,judgement,cmp_coursecd,cmp_majorcd,cmp_examcoursecd)

