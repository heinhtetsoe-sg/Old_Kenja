-- $Id: rep-schreg_base_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_BASE_MST_BACK

create table SCHREG_BASE_MST_BACK like SCHREG_BASE_MST

insert into SCHREG_BASE_MST_BACK select * from SCHREG_BASE_MST

drop table SCHREG_BASE_MST

create table SCHREG_BASE_MST \
    (SCHREGNO               varchar(8)    not null, \
     INOUTCD                varchar(1), \
     NAME                   varchar(60), \
     NAME_SHOW              varchar(30), \
     NAME_KANA              varchar(120), \
     NAME_KANA_SHOW         varchar(60), \
     NAME_ENG               varchar(40), \
     BIRTHDAY               date, \
     SEX                    varchar(1), \
     BLOODTYPE              varchar(2), \
     BLOOD_RH               varchar(1), \
     CLAIM_SEND             varchar(1), \
     APPLICANTNO            varchar(7), \
     CURRICULUM_YEAR        varchar(4), \
     SPECIAL_DIV            varchar(1), \
     EDUCATION_REC_GET_FLG  varchar(1), \
     EDUCATION_REC_PUT_FLG  varchar(1), \
     MOBILE_PHONE_NO        varchar(14), \
     FINSCHOOLCD            varchar(11), \
     FINISH_DATE            date, \
     PRISCHOOLCD            varchar(7), \
     SCHREG_DATE            date, \
     ENT_DATE               date, \
     ENT_DIV                varchar(1), \
     ENT_REASON             varchar(75), \
     ENT_SCHOOL             varchar(75), \
     ENT_ADDR               varchar(75), \
     GRD_DATE               date, \
     GRD_DIV                varchar(1), \
     GRD_REASON             varchar(75), \
     GRD_SCHOOL             varchar(75), \
     GRD_ADDR               varchar(75), \
     GRD_NO                 varchar(8), \
     GRD_TERM               varchar(4), \
     GRD_SCHEDULE_DATE      date, \
     GRD_RECOGNIT_FLG       varchar(1), \
     REMARK1                varchar(75), \
     REMARK2                varchar(75), \
     REMARK3                varchar(75), \
     EMERGENCYCALL          varchar(60), \
     EMERGENCYNAME          varchar(60), \
     EMERGENCYRELA_NAME     varchar(30), \
     EMERGENCYTELNO         varchar(14), \
     EMERGENCYCALL2         varchar(60), \
     EMERGENCYNAME2         varchar(60), \
     EMERGENCYRELA_NAME2    varchar(30), \
     EMERGENCYTELNO2        varchar(14), \
     REGISTERCD             varchar(8), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_BASE_MST add constraint pk_schreg_base_mst primary key (SCHREGNO)


insert into SCHREG_BASE_MST \
select \
    SCHREGNO, \
    INOUTCD, \
    NAME, \
    NAME_SHOW, \
    NAME_KANA, \
    NAME_KANA_SHOW, \
    NAME_ENG, \
    BIRTHDAY, \
    SEX, \
    BLOODTYPE, \
    BLOOD_RH, \
    CLAIM_SEND, \
    APPLICANTNO, \
    CURRICULUM_YEAR, \
    SPECIAL_DIV, \
    EDUCATION_REC_GET_FLG, \
    EDUCATION_REC_PUT_FLG, \
    MOBILE_PHONE_NO, \
    FINSCHOOLCD, \
    FINISH_DATE, \
    PRISCHOOLCD, \
    cast(null as date) AS SCHREG_DATE, \
    ENT_DATE, \
    ENT_DIV, \
    ENT_REASON, \
    ENT_SCHOOL, \
    ENT_ADDR, \
    GRD_DATE, \
    GRD_DIV, \
    GRD_REASON, \
    GRD_SCHOOL, \
    GRD_ADDR, \
    GRD_NO, \
    GRD_TERM, \
    GRD_SCHEDULE_DATE, \
    GRD_RECOGNIT_FLG, \
    REMARK1, \
    REMARK2, \
    REMARK3, \
    EMERGENCYCALL, \
    EMERGENCYNAME, \
    EMERGENCYRELA_NAME, \
    EMERGENCYTELNO, \
    EMERGENCYCALL2, \
    EMERGENCYNAME2, \
    EMERGENCYRELA_NAME2, \
    EMERGENCYTELNO2, \
    REGISTERCD, \
    UPDATED \
FROM \
    SCHREG_BASE_MST_BACK
