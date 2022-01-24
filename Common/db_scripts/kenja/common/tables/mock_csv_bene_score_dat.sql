-- $Id: 7935d3d853b82848dd126c5fd03dca94cd5f0864 $

drop table MOCK_CSV_BENE_SCORE_DAT
create table MOCK_CSV_BENE_SCORE_DAT( \
    YEAR                varchar(4)  not null, \
    KYOUZAICD           varchar(2)  not null, \
    BENEID              varchar(10)  not null, \
    SEQ                 varchar(3)  not null, \
    MOCK_SUBCLASS_CD    varchar(6), \
    SCORE               smallint, \
    GTZ                 varchar(5), \
    ALL_DEV             decimal(5,1), \
    SCHOOL_DEV          decimal(5,1), \
    ALL_RANK            integer, \
    SCHOOL_RANK         integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_BENE_SCORE_DAT add constraint PK_BENE_SCORE_D primary key (YEAR, KYOUZAICD, BENEID, SEQ)
