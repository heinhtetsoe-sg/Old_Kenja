-- $Id: 1116b24a5add4dcda7f650c1f58a6d881d4ca67b $

drop table MOCK_CSV_KAWAI_SCORE_HEAD_DAT
create table MOCK_CSV_KAWAI_SCORE_HEAD_DAT( \
    YEAR                varchar(4)  not null, \
    MOSI_CD             varchar(5)  not null, \
    SEQ                 varchar(3)  not null, \
    TYEAR               varchar(120), \
    TMOSI_CD            varchar(120), \
    TKAMOKU_CD          varchar(120), \
    TKAMOKU_NAME        varchar(120), \
    THAITEN             varchar(120), \
    TSCORE              varchar(120), \
    TDEVIATION          varchar(120), \
    TLEVEL              varchar(120), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_KAWAI_SCORE_HEAD_DAT add constraint PK_KAWAI_SCORE_HD primary key (YEAR, MOSI_CD, SEQ)
