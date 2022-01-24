-- $Id: 6e143f781dce08f0cdc777e779598b78e0e10089 $

drop table MOCK_CSV_SUNDAI_HOPE_HEAD_DAT
create table MOCK_CSV_SUNDAI_HOPE_HEAD_DAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(4)  not null, \
    SEQ             varchar(3)  not null, \
    TYEAR           varchar(120), \
    TMOSI_CD        varchar(120), \
    TSCHOOL_CD      varchar(120), \
    TSCHOOL_NAME    varchar(120), \
    TNITTEI         varchar(120), \
    TRANK           varchar(120), \
    TCNT            varchar(120), \
    TJUDGE_HYOUKA   varchar(120), \
    TJUDGE_SUUTI    varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_SUNDAI_HOPE_HEAD_DAT add constraint PK_SUN_HOPE_HD primary key (YEAR, MOSI_CD, SEQ)
