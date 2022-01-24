-- kanji=漢字
-- $Id: 7e82972b7c15d80e1a912e00fc65f24b2d6c9001 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
