-- $Id: 9120d007c87e0a4b332a5678f657c1a239b8207d $

drop table MOCK_CSV_ZKAI_HOPE_HEAD_DAT
create table MOCK_CSV_ZKAI_HOPE_HEAD_DAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(4)  not null, \
    SEQ             varchar(3)  not null, \
    TYEAR           varchar(120), \
    TMOSI_CD        varchar(120), \
    TSCHOOL_NAME    varchar(120), \
    TSCHOOL_CD      varchar(120), \
    TJUDGE_HYOUKA   varchar(120), \
    TRANK           varchar(120), \
    TJUDGE_SUUTI    varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_ZKAI_HOPE_HEAD_DAT add constraint PK_ZKAI_HOPE_HD primary key (YEAR, MOSI_CD, SEQ)
