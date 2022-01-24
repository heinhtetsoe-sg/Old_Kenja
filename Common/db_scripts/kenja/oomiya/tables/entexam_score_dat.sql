drop table entexam_score_dat

create table entexam_score_dat \
( \
    entexamyear     varchar(4)  not null, \
    applicantdiv    varchar(1)  not null, \
    testdiv         varchar(1)  not null, \
    exam_type       varchar(1)  not null, \
    receptno        varchar(4)  not null, \
    testsubclasscd  varchar(1)  not null, \
    attend_flg      varchar(1), \
    score           smallint, \
    std_score       decimal(4,1), \
    rank            smallint, \
    registercd      varchar(8),  \
    updated         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_score_dat add constraint \
pk_entexam_score primary key (entexamyear,applicantdiv,testdiv,exam_type,receptno,testsubclasscd)
