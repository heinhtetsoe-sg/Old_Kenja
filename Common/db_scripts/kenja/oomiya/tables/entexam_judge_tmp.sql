drop table entexam_judge_tmp

create table entexam_judge_tmp \
( \
    entexamyear         varchar(4)  not null, \
    applicantdiv        varchar(1)  not null, \
    testdiv             varchar(1)  not null, \
    exam_type           varchar(1)  not null, \
    receptno            varchar(4)  not null, \
    examno              varchar(5)  not null, \
    judge_exam_type     varchar(1), \
    judgediv            varchar(1), \
    judge_shdiv         varchar(1), \
    registercd          varchar(10), \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_judge_tmp add constraint \
pk_entexam_judge primary key (entexamyear,applicantdiv,testdiv,exam_type,receptno)
