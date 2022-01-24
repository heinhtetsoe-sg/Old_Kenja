-- $Id: 785d293223656f165b4185e3219ef25074de25a5 $

drop table REDUCTION_SCHOOL_TARGET_MST

create table REDUCTION_SCHOOL_TARGET_MST( \
    SCHOOLCD            varchar(12)   not null, \
    SCHOOL_KIND         varchar(2)    not null, \
    YEAR                varchar(4)    not null, \
    REDUCTION_DIV_CD    varchar(2)    not null, \
    REDUCTION_TARGET    varchar(1)    not null, \
    MONEY_DIV           varchar(1), \
    NUMERATOR           smallint, \
    DENOMINATOR         smallint, \
    MONEY               integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SCHOOL_TARGET_MST add constraint PK_REDUC_SCHL_TARG primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_DIV_CD, REDUCTION_TARGET)