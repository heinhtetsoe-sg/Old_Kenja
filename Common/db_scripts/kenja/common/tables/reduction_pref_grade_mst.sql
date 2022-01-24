-- $Id: e89e6546c618a7d7e3c064ea42e78108773e8c3d $

drop table REDUCTION_PREF_GRADE_MST
create table REDUCTION_PREF_GRADE_MST( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    YEAR                varchar(4)  not null, \
    PREFECTURESCD       varchar(2)  not null, \
    GRADE               varchar(2)  not null, \
    REFER_YEAR_DIV1     varchar(2), \
    TOTALL_MONEY_1      integer, \
    REFER_YEAR_DIV2     varchar(2), \
    TOTALL_MONEY_2      integer, \
    USE_RANK            varchar(1), \
    ZENKI_KAISI_YEAR    varchar(4), \
    KOUKI_KAISI_YEAR    varchar(4), \
    STANDARD_SCHOOL_FEE integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_PREF_GRADE_MST add constraint PK_REDUC_PREFGRADE primary key(SCHOOLCD, SCHOOL_KIND, YEAR, PREFECTURESCD, GRADE)
