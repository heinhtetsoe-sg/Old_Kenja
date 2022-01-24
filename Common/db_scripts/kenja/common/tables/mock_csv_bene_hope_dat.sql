-- $Id: 497fa5ac3a9c16d24f37938207817b45c0ae282b $

drop table MOCK_CSV_BENE_HOPE_DAT
create table MOCK_CSV_BENE_HOPE_DAT( \
    YEAR            varchar(4)  not null, \
    KYOUZAICD       varchar(2)  not null, \
    BENEID          varchar(10)  not null, \
    SEQ             varchar(3)  not null, \
    SCHOOL_CD       varchar(10), \
    BOSYUTANNI      varchar(120), \
    SCHOOL_NAME     varchar(120), \
    GAKUBU_NAME     varchar(120), \
    GAKKA_NAME      varchar(120), \
    NITTEI          varchar(9), \
    HOUSIKI         varchar(30), \
    ALL_JUDGE       varchar(1), \
    JUDGE1          varchar(1), \
    JUDGE2          varchar(1), \
    ALL_JUDGE_SS    varchar(15), \
    JUDGE1_SS       varchar(15), \
    JUDGE2_SS       varchar(15), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_BENE_HOPE_DAT add constraint PK_BENE_HOPE_D primary key (YEAR, KYOUZAICD, BENEID, SEQ)
