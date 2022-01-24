drop table entexam_desire_dat

create table entexam_desire_dat \
( \
    entexamyear         varchar(4)  not null, \
    applicantdiv        varchar(1)  not null, \
    testdiv             varchar(1)  not null, \
    examno              varchar(5)  not null, \
    applicant_div       varchar(1), \
    examinee_div        varchar(1), \
    registercd          varchar(8), \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_desire_dat add constraint \
pk_entexam_desire primary key (entexamyear,applicantdiv,testdiv,examno)
