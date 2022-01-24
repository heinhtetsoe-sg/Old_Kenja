-- $Id: 598dd422eba3f212f0867958fb31f262b6cd974d $

drop table MOCK_CSV_BENE_HOPE_HDAT_OLD
create table MOCK_CSV_BENE_HOPE_HDAT_OLD like MOCK_CSV_BENE_HOPE_HDAT
insert into  MOCK_CSV_BENE_HOPE_HDAT_OLD select * from MOCK_CSV_BENE_HOPE_HDAT

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

insert into MOCK_CSV_BENE_HOPE_HDAT \
    SELECT \
        YEAR, \
        KYOUZAICD, \
        MOCKCD, \
        TYPE, \
        KYOUZAINAME, \
        GAKKACD, \
        GAKKANAME, \
        BENEID, \
        CAST(NULL AS VARCHAR(8)) AS SCHREGNO, \
        HR_CLASS, \
        ATTENDNO, \
        NAME, \
        BUNRI_DIV, \
        BIRTHDAY, \
        SEX, \
        REGISTERCD, \
        UPDATED \
    FROM \
        MOCK_CSV_BENE_HOPE_HDAT_OLD
