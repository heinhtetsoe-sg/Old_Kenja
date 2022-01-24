-- $Id: 5eaa092b83a3783fa02abfb5d8c8a532ac294f04 $

drop table STAFF_DETAIL_EXT_MST
create table STAFF_DETAIL_EXT_MST( \
    YEAR           varchar(4)    not null, \
    STAFFCD        varchar(10)    not null, \
    STAFF_SEQ      varchar(3)    not null, \
    EXT_SEQ        smallint      not null, \
    FIELD1         varchar(15), \
    FIELD2         varchar(15), \
    FIELD3         varchar(120), \
    FIELD4         varchar(120), \
    FIELD5         varchar(120), \
    REGISTERCD     varchar(10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table STAFF_DETAIL_EXT_MST add constraint PK_STF_DETAIL_EXT primary key (YEAR, STAFFCD, STAFF_SEQ, EXT_SEQ)