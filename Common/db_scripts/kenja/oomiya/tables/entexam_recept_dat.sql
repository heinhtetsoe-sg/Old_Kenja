drop table entexam_recept_dat

create table entexam_recept_dat \
( \
    entexamyear         varchar(4)  not null, \
    applicantdiv        varchar(1)  not null, \
    testdiv             varchar(1)  not null, \
    exam_type           varchar(1)  not null, \
    receptno            varchar(4)  not null, \
    examno              varchar(5)  not null, \
    attend_all_flg      varchar(1), \
    total2              smallint, \
    avarage2            decimal(4,1), \
    total_rank2         smallint, \
    div_rank2           smallint, \
    total4              smallint, \
    avarage4            decimal(4,1), \
    total_rank4         smallint, \
    div_rank4           smallint, \
    judge_exam_type     varchar(1), \
    judgediv            varchar(1), \
    honordiv            varchar(1), \
    adjournmentdiv      varchar(1), \
    judgeline           varchar(1), \
    judgeclass          varchar(1), \
    katen               smallint, \
    registercd          varchar(8), \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_recept_dat add constraint \
pk_entexam_rcpt primary key (entexamyear,applicantdiv,testdiv,exam_type,receptno)
