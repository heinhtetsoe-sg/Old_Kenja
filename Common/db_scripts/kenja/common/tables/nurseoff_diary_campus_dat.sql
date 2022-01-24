-- kanji=����
-- $Id: 7e82972b7c15d80e1a912e00fc65f24b2d6c9001 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table NURSEOFF_DIARY_CAMPUS_DAT

create table NURSEOFF_DIARY_CAMPUS_DAT( \
    SCHOOLCD            varchar(12)         not null, \
    SCHOOL_KIND         varchar(2)          not null, \
    CAMPUS_DIV          varchar(2)          not null, \
    DATE                date                not null, \
    WEATHER             varchar(1), \
    WEATHER_TEXT        varchar(30), \
    TEMPERATURE         varchar(5), \
    EVENT               varchar(226), \
    DIARY               varchar(2000), \
    YEAR                varchar(4), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table NURSEOFF_DIARY_CAMPUS_DAT add constraint pk_nurse_diary_cam primary key \
        (SCHOOLCD,SCHOOL_KIND,CAMPUS_DIV,DATE)
