-- kanji=����
-- $Id: 93a76d50966889e92d4ed0cb7ad4916389d054cd $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table NURSEOFF_DIARY_DAT

create table NURSEOFF_DIARY_DAT( \
    SCHOOLCD            varchar(12)         not null, \
    SCHOOL_KIND         varchar(2)          not null, \
    DATE                date                not null, \
    WEATHER             varchar(1), \
    WEATHER_TEXT        varchar(30), \
    TEMPERATURE         varchar(5), \
    EVENT               varchar(1000), \
    DIARY               varchar(3100), \
    YEAR                varchar(4), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp, \
    HUMIDITY            varchar(3), \
    CHECK_HOUR          varchar(2), \
    CHECK_MINUTE        varchar(2), \
    COLOR               varchar(2), \
    TURBIDITY           varchar(2), \
    SMELL               varchar(2), \
    TASTE               varchar(2), \
    RESIDUAL_CHLORINE   varchar(4), \
    WATER_REMARK        varchar(15), \
    AED                 varchar(2) \
) in usr1dms index in idx1dms

alter table NURSEOFF_DIARY_DAT add constraint pk_nurse_diary_dat primary key \
        (SCHOOLCD,SCHOOL_KIND,DATE)
