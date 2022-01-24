-- $Id: c405f1407753a2731b2f036f53378b9ecc547240 $

drop table QUALIFIED_SETUP_DAT
create table QUALIFIED_SETUP_DAT( \
    YEAR                varchar(4)    not null, \
    QUALIFIED_CD        varchar(4)    not null, \
    SETUP_QUALIFIED_CD  varchar(4)    not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table QUALIFIED_SETUP_DAT add constraint PK_QUALIFIED_SET_D primary key (YEAR, QUALIFIED_CD, SETUP_QUALIFIED_CD)
