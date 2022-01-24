-- $Id: 4b7fb475014abdcb767f0804c2e031ec43f3a193 $

drop table SCHREG_BASE_MST_BACK

create table SCHREG_BASE_MST_BACK like SCHREG_BASE_MST

insert into SCHREG_BASE_MST_BACK select * from SCHREG_BASE_MST

drop table SCHREG_BASE_MST

create table SCHREG_BASE_MST \
    (SCHREGNO            varchar(8)    not null, \
     INOUTCD             varchar(1), \
     NAME                varchar(120), \
     NAME_SHOW           varchar(120), \
     NAME_KANA           varchar(240), \
     NAME_ENG            varchar(40), \
     REAL_NAME           varchar(120), \
     REAL_NAME_KANA      varchar(240), \
     BIRTHDAY            date, \
     SEX                 varchar(1), \
     BLOODTYPE           varchar(2), \
     BLOOD_RH            varchar(1), \
     HANDICAP            varchar(3), \
     NATIONALITY         varchar(3), \
     FINSCHOOLCD         varchar(12), \
     FINISH_DATE         date, \
     PRISCHOOLCD         varchar(7), \
     ENT_DATE            date, \
     ENT_DIV             varchar(1), \
     ENT_REASON          varchar(75), \
     ENT_SCHOOL          varchar(75), \
     ENT_ADDR            varchar(150), \
     ENT_ADDR2           varchar(150), \
     GRD_DATE            date, \
     GRD_DIV             varchar(1), \
     GRD_REASON          varchar(75), \
     GRD_SCHOOL          varchar(75), \
     GRD_ADDR            varchar(150), \
     GRD_ADDR2           varchar(150), \
     GRD_NO              varchar(8), \
     GRD_TERM            varchar(4), \
     REMARK1             varchar(150), \
     REMARK2             varchar(150), \
     REMARK3             varchar(150), \
     EMERGENCYCALL       varchar(60), \
     EMERGENCYNAME       varchar(60), \
     EMERGENCYRELA_NAME  varchar(30), \
     EMERGENCYTELNO      varchar(14), \
     EMERGENCYCALL2      varchar(60), \
     EMERGENCYNAME2      varchar(60), \
     EMERGENCYRELA_NAME2 varchar(30), \
     EMERGENCYTELNO2     varchar(14), \
     REGISTERCD          varchar(10), \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SCHREG_BASE_MST add constraint pk_schreg_base_mst primary key (schregno)


insert into SCHREG_BASE_MST \
select \
     * \
FROM \
    SCHREG_BASE_MST_BACK
