-- $Id: 4780ce5c176c41678c135edf9f6bfdca11c57b4f $

drop table MOCK_CSV_ZKAI_SCORE_DAT
create table MOCK_CSV_ZKAI_SCORE_DAT( \
    YEAR                varchar(4)  not null, \
    MOSI_CD             varchar(4)  not null, \
    HR_CLASS            varchar(3)  not null, \
    ATTENDNO            varchar(3)  not null, \
    SEQ                 varchar(3)  not null, \
    MOCK_SUBCLASS_CD    varchar(6), \
    SCORE               smallint, \
    DEVIATION           decimal(5,1), \
    RANK                integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_ZKAI_SCORE_DAT add constraint PK_ZKAI_SCORE_D primary key (YEAR, MOSI_CD, HR_CLASS, ATTENDNO, SEQ)
