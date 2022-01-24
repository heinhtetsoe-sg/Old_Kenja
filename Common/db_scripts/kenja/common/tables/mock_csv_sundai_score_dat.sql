-- $Id: c8d79f78a4f6e878748f03787e6925f1f6ad82aa $

drop table MOCK_CSV_SUNDAI_SCORE_DAT
create table MOCK_CSV_SUNDAI_SCORE_DAT( \
    YEAR                varchar(4)  not null, \
    MOSI_CD             varchar(4)  not null, \
    EXAMNO              varchar(6)  not null, \
    SEQ                 varchar(3)  not null, \
    MOCK_SUBCLASS_CD    varchar(6), \
    SCORE               smallint, \
    DEVIATION           decimal(5,1), \
    RANK                integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_SUNDAI_SCORE_DAT add constraint PK_SUN_SCORE_D primary key (YEAR, MOSI_CD, EXAMNO, SEQ)
