-- kanji=´Á»ú
-- $Id: entexam_judge_tmp.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_judge_tmp

create table entexam_judge_tmp \
( \
    entexamyear             varchar(4)  not null, \
    testdiv             varchar(1)  not null, \
    examno              varchar(4)  not null, \
    coursecd            varchar(1)  not null, \
    majorcd             varchar(3)  not null, \
    examcoursecd        varchar(4)  not null, \
    judgement           varchar(1), \
    regularsuccess_flg  varchar(1), \
    registercd          varchar(8), \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_judge_tmp add constraint \
pk_entexam_judge primary key (entexamyear,testdiv,examno,coursecd,majorcd,examcoursecd)
