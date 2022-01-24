-- $Id: b003fb37ef6f500da708e71f1a3fdf4866d43c1a $

drop table QUALIFIED_SETUP_HDAT
create table QUALIFIED_SETUP_HDAT( \
    YEAR                varchar(4)    not null, \
    QUALIFIED_CD        varchar(4)    not null, \
    LIMIT_MONTH         smallint, \
    SETUP_CNT           smallint, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table QUALIFIED_SETUP_HDAT add constraint PK_QUALIFIED_SET_H primary key (YEAR, QUALIFIED_CD)
