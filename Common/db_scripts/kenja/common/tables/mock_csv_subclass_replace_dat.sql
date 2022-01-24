-- $Id: 3d80951748892aee291e284b3baec48007b8e854 $

drop table MOCK_CSV_SUBCLASS_REPLACE_DAT
create table MOCK_CSV_SUBCLASS_REPLACE_DAT( \
    YEAR                varchar(4)  not null, \
    MOCKCD              varchar(9)  not null, \
    GRADE               varchar(2)  not null, \
    FIELD_CNT           smallint  not null, \
    MOCK_SUBCLASS_CD    varchar(6) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_SUBCLASS_REPLACE_DAT add constraint PK_SUB_REP primary key (YEAR, MOCKCD, GRADE, FIELD_CNT)
