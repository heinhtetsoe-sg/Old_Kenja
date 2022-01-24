-- $Id: 5bd727ac6972ee6f944e15002286e3a17fd16f9c $

DROP TABLE SCHREG_DOMITORY_TEMPORARY_OUT_DAT

CREATE TABLE SCHREG_DOMITORY_TEMPORARY_OUT_DAT ( \
    YEAR              varchar(4) not null, \
    MONTH             varchar(2) not null, \
    SCHREGNO          varchar(8) not null, \
    TEMPORARY_OUT_FLG varchar(1), \
    REGISTERCD        varchar(10), \
    UPDATED           timestamp default current timestamp \
)

alter table SCHREG_DOMITORY_TEMPORARY_OUT_DAT add constraint PK_DOMITORYTEMPOUT \
primary key (YEAR, MONTH, SCHREGNO)
