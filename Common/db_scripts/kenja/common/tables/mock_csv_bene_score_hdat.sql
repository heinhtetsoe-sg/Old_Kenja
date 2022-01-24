-- $Id: 22fbcd00db1b84214aac25d0d5698023f49879ce $

drop table MOCK_CSV_BENE_SCORE_HDAT
create table MOCK_CSV_BENE_SCORE_HDAT( \
    YEAR        varchar(4)  not null, \
    KYOUZAICD   varchar(2)  not null, \
    MOCKCD      varchar(9)  not null, \
    TYPE        varchar(10), \
    KYOUZAINAME varchar(120), \
    GAKKACD     varchar(8), \
    GAKKANAME   varchar(30), \
    BENEID      varchar(10)  not null, \
    HR_CLASS    varchar(3), \
    ATTENDNO    varchar(3), \
    NAME        varchar(120), \
    BUNRI_DIV   varchar(1), \
    BIRTHDAY    varchar(8), \
    SEX         varchar(1), \
    DEVIATION   decimal(5,1), \
    RANK        integer, \
    CNT         integer, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_BENE_SCORE_HDAT add constraint PK_BENE_SCORE_H primary key (YEAR, KYOUZAICD, BENEID)
