-- $Id: dc87cf0b1024f06d5e3f067872e0b180e41d1077 $

drop table MOCK_CSV_SUNDAI_HOPE_DAT
create table MOCK_CSV_SUNDAI_HOPE_DAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(4)  not null, \
    EXAMNO          varchar(6)  not null, \
    SEQ             varchar(3)  not null, \
    SCHOOL_CD       varchar(10), \
    SCHOOL_NAME     varchar(120), \
    NITTEI          varchar(9), \
    RANK            integer, \
    CNT             integer, \
    JUDGE_HYOUKA    varchar(15), \
    JUDGE_SUUTI     decimal(5,1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_SUNDAI_HOPE_DAT add constraint PK_SUN_HOPE_D primary key (YEAR, MOSI_CD, EXAMNO, SEQ)
