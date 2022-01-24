-- $Id: e93327489b7512f4b461bfcf4f5301242844bbe6 $

drop table MOCK_CSV_ZKAI_HOPE_DAT
create table MOCK_CSV_ZKAI_HOPE_DAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(4)  not null, \
    HR_CLASS        varchar(3)  not null, \
    ATTENDNO        varchar(3)  not null, \
    SEQ             varchar(3)  not null, \
    SCHOOL_NAME     varchar(120), \
    SCHOOL_CD       varchar(10), \
    JUDGE_HYOUKA    varchar(15), \
    RANK            integer, \
    JUDGE_SUUTI     decimal(5,1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_ZKAI_HOPE_DAT add constraint PK_ZKAI_HOPE_D primary key (YEAR, MOSI_CD, HR_CLASS, ATTENDNO, SEQ)
