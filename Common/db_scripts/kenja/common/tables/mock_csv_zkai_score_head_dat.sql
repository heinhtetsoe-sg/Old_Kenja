-- $Id: c5f5ce974e98eb4a86918709480fe5975afb9428 $

drop table MOCK_CSV_ZKAI_SCORE_HEAD_DAT
create table MOCK_CSV_ZKAI_SCORE_HEAD_DAT( \
    YEAR                varchar(4)  not null, \
    MOSI_CD             varchar(4)  not null, \
    SEQ                 varchar(3)  not null, \
    MOCK_SUBCLASS_CD    varchar(6), \
    TYEAR               varchar(120), \
    TMOSI_CD            varchar(120), \
    TSCORE              varchar(120), \
    TDEVIATION          varchar(120), \
    TRANK               varchar(120), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_ZKAI_SCORE_HEAD_DAT add constraint PK_ZKAI_SCORE_HD primary key (YEAR, MOSI_CD, SEQ)
