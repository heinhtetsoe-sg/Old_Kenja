-- $Id: 9a151d964ea550d14152a81e4585c79b528d4d1e $

drop table MOCK_CSV_KAWAI_SCORE_DAT
create table MOCK_CSV_KAWAI_SCORE_DAT( \
    YEAR                varchar(4)  not null, \
    MOSI_CD             varchar(5)  not null, \
    GRADE               varchar(2)  not null, \
    HR_CLASS            varchar(3)  not null, \
    ATTENDNO            varchar(3)  not null, \
    SEQ                 varchar(3)  not null, \
    MOCK_SUBCLASS_CD    varchar(6), \
    KAMOKU_CD           varchar(6), \
    KAMOKU_NAME         varchar(120), \
    HAITEN              smallint, \
    SCORE               smallint, \
    DEVIATION           decimal(5,1), \
    LEVEL               varchar(30), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_KAWAI_SCORE_DAT add constraint PK_KAWAI_SCORE_D primary key (YEAR, MOSI_CD, GRADE, HR_CLASS, ATTENDNO, SEQ)
