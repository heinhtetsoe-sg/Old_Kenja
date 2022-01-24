-- $Id: 8843e98bd72490644396b49ff2eb2efe5c0b926e $

drop table MOCK_CSV_KAWAI_HOPE_DAT
create table MOCK_CSV_KAWAI_HOPE_DAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(5)  not null, \
    GRADE           varchar(2)  not null, \
    HR_CLASS        varchar(3)  not null, \
    ATTENDNO        varchar(3)  not null, \
    SEQ             varchar(3)  not null, \
    SCHOOL_CD_5     varchar(5), \
    SCHOOL_CD_10    varchar(10), \
    SCHOOL_NAME     varchar(120), \
    HYOUKA_SEISEKI  varchar(15), \
    SE_HYOUKA       varchar(15), \
    NI_HYOUKA       varchar(15), \
    SOUHYOU_PO      varchar(15), \
    SOU_HYOUKA      varchar(15), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_KAWAI_HOPE_DAT add constraint PK_KAWAI_HOPE_D primary key (YEAR, MOSI_CD, GRADE, HR_CLASS, ATTENDNO, SEQ)
