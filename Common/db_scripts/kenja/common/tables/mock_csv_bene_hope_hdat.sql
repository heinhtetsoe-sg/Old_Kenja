-- $Id: 7622fca965d7e50919c25c87bab862020f46d47a $

drop table MOCK_CSV_BENE_HOPE_HDAT
create table MOCK_CSV_BENE_HOPE_HDAT( \
    YEAR        varchar(4)  not null, \
    KYOUZAICD   varchar(2)  not null, \
    MOCKCD      varchar(9)  not null, \
    TYPE        varchar(10), \
    KYOUZAINAME varchar(120), \
    GAKKACD     varchar(8), \
    GAKKANAME   varchar(30), \
    BENEID      varchar(10)  not null, \
    SCHREGNO    varchar(8), \
    HR_CLASS    varchar(3), \
    ATTENDNO    varchar(3), \
    NAME        varchar(120), \
    BUNRI_DIV   varchar(1), \
    BIRTHDAY    varchar(8), \
    SEX         varchar(1), \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_BENE_HOPE_HDAT add constraint PK_BENE_HOPE_H primary key (YEAR, KYOUZAICD, BENEID)
