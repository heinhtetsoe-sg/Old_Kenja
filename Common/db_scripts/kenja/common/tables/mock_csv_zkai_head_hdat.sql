-- $Id: aa28683e3eb1074db50c9a345af21e47ebdb1804 $

drop table MOCK_CSV_ZKAI_HEAD_HDAT
create table MOCK_CSV_ZKAI_HEAD_HDAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(4)  not null, \
    MOCKCD          varchar(9)  not null, \
    TYEAR           varchar(120), \
    TMOSI_CD        varchar(120), \
    TMOSI_NAME      varchar(120), \
    THR_CLASS       varchar(120), \
    TATTENDNO       varchar(120), \
    TKANA           varchar(120), \
    TDEVIATION      varchar(120), \
    TRANK           varchar(120), \
    TCNT            varchar(120), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_ZKAI_HEAD_HDAT add constraint PK_ZKAI_HH primary key (YEAR, MOSI_CD)
