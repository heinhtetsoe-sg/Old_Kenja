-- $Id: a64931bb45dcc8b0ad788f3b423364fb3b1ae6b7 $

drop table MOCK_CSV_BENE_SCORE_HEAD_DAT
create table MOCK_CSV_BENE_SCORE_HEAD_DAT( \
    YEAR                varchar(4)  not null, \
    KYOUZAICD           varchar(2)  not null, \
    SEQ                 varchar(3)  not null, \
    MOCK_SUBCLASS_CD    varchar(6), \
    TYEAR               varchar(120), \
    TKYOUZAI            varchar(120), \
    TSCORE              varchar(120), \
    TGTZ                varchar(120), \
    TALL_DEV            varchar(120), \
    TSCHOOL_DEV         varchar(120), \
    TALL_RANK           varchar(120), \
    TSCHOOL_RANK        varchar(120), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_BENE_SCORE_HEAD_DAT add constraint PK_BENE_SCORE_HD primary key (YEAR, KYOUZAICD, SEQ)
