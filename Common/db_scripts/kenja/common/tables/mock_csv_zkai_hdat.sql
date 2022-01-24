-- $Id: 14fa2f55d2204956aa945072e0275171e67eba0f $

drop table MOCK_CSV_ZKAI_HDAT
create table MOCK_CSV_ZKAI_HDAT( \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(4)  not null, \
    MOCKCD          varchar(9)  not null, \
    MOSI_NAME       varchar(45), \
    HR_CLASS        varchar(3)  not null, \
    ATTENDNO        varchar(3)  not null, \
    KANA            varchar(120), \
    DEVIATION       decimal(5,1), \
    RANK            integer, \
    CNT             integer, \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_ZKAI_HDAT add constraint PK_ZKAI_H primary key (YEAR, MOSI_CD, HR_CLASS, ATTENDNO)
