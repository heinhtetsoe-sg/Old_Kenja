-- $Id: entexam_applicantbase_dat.sql 64442 2019-01-10 03:07:11Z yamashiro $
drop table entexam_applicantbase_dat

create table entexam_applicantbase_dat \
( \
    entexamyear         varchar(4)  not null, \
    applicantdiv        varchar(1)  not null, \
    examno              varchar(5)  not null, \
    testdiv             varchar(1)  not null, \
    shdiv               varchar(1)  not null, \
    desirediv           varchar(1)  not null, \
    testdiv0            varchar(1), \
    testdiv1            varchar(1), \
    testdiv2            varchar(1), \
    testdiv3            varchar(1), \
    testdiv4            varchar(1), \
    testdiv5            varchar(1), \
    testdiv6            varchar(1), \
    name                varchar(60), \
    name_kana           varchar(120), \
    sex                 varchar(1), \
    eracd               varchar(1), \
    birth_y             varchar(2), \
    birth_m             varchar(2), \
    birth_d             varchar(2), \
    birthday            date, \
    fs_cd               varchar(7), \
    fs_name             varchar(75), \
    fs_area_cd          varchar(2), \
    fs_grdyear          varchar(4), \
    interview_attend_flg varchar(1), \
    suc_coursecd        varchar(1), \
    suc_majorcd         varchar(3), \
    suc_coursecode      varchar(4), \
    judgement           varchar(1), \
    special_measures    varchar(1), \
    procedurediv        varchar(1), \
    entdiv              varchar(1), \
    entclass            varchar(1), \
    honordiv            varchar(1), \
    success_noticeno    varchar(4), \
    failure_noticeno    varchar(4), \
    remark1             varchar(60), \
    remark2             varchar(60), \
    recom_examno1       varchar(5), \
    recom_examno2       varchar(5), \
    recom_examno3       varchar(5), \
    registercd          varchar(8),  \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_applicantbase_dat add constraint \
pk_entexam_app primary key (entexamyear,examno)

