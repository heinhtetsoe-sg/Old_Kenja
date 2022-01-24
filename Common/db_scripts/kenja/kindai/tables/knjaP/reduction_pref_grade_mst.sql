-- $Id: reduction_pref_grade_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table REDUCTION_PREF_GRADE_MST
create table REDUCTION_PREF_GRADE_MST( \
    YEAR                varchar(4) not null, \
    PREFECTURESCD       varchar(2) not null, \
    GRADE               varchar(2) not null, \
    CURRICULUM_FLG      varchar(1), \
    THIS_YEAR_FLG       varchar(1), \
    USE_RANK            varchar(1), \
    ZENKI_KAISI_YEAR    varchar(4), \
    KOUKI_KAISI_YEAR    varchar(4), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_PREF_GRADE_MST add constraint PK_REDUC_PREFGRADE primary key(YEAR, PREFECTURESCD, GRADE)
