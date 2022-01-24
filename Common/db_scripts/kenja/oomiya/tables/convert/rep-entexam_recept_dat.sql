-- $Id: rep-entexam_recept_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table entexam_recept_dat_old
create table entexam_recept_dat_old like entexam_recept_dat
insert into entexam_recept_dat_old select * from entexam_recept_dat

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


insert into entexam_recept_dat \
( \
    entexamyear         , \
    applicantdiv        , \
    testdiv             , \
    exam_type           , \
    receptno            , \
    examno              , \
    attend_all_flg      , \
    total2              , \
    avarage2            , \
    total_rank2         , \
    div_rank2           , \
    total4              , \
    avarage4            , \
    total_rank4         , \
    div_rank4           , \
    judge_exam_type     , \
    judgediv            , \
    honordiv            , \
    adjournmentdiv      , \
    judgeline           , \
    judgeclass          , \
    katen               , \
    registercd          , \
    updated             \
     ) \
select \
    entexamyear         , \
    applicantdiv        , \
    testdiv             , \
    exam_type           , \
    receptno            , \
    examno              , \
    attend_all_flg      , \
    total2              , \
    avarage2            , \
    total_rank2         , \
    div_rank2           , \
    total4              , \
    avarage4            , \
    total_rank4         , \
    div_rank4           , \
    judge_exam_type     , \
    judgediv            , \
    honordiv            , \
    adjournmentdiv      , \
    judgeline           , \
    judgeclass          , \
    cast(null as smallint), \
    registercd          , \
    updated             \
from entexam_recept_dat_old 