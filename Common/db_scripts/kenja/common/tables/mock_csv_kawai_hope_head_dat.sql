-- $Id: 7936c9a4cab87a9628daf174b40ff15d8b5cd239 $

drop table MOCK_CSV_KAWAI_HOPE_HEAD_DAT
create table MOCK_CSV_KAWAI_HOPE_HEAD_DAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(5)  not null, \
    SEQ             varchar(3)  not null, \
    TYEAR           varchar(120), \
    TMOSI_CD        varchar(120), \
    TSCHOOL_CD_5    varchar(120), \
    TSCHOOL_CD_10   varchar(120), \
    TSCHOOL_NAME    varchar(120), \
    THYOUKA_SEISEKI varchar(120), \
    TSE_HYOUKA      varchar(120), \
    TNI_HYOUKA      varchar(120), \
    TSOUHYOU_PO     varchar(120), \
    TSOU_HYOUKA     varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_KAWAI_HOPE_HEAD_DAT add constraint PK_KAWAI_HOPE_HD primary key (YEAR, MOSI_CD, SEQ)
