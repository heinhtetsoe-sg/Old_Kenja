-- $Id: 2fa3da527a178956fc5fbbdd72dcce41aaf89893 $

drop table REDUCTION_SCHOOL_MST

create table REDUCTION_SCHOOL_MST( \
    SCHOOLCD            varchar(12)   not null, \
    SCHOOL_KIND         varchar(2)    not null, \
    YEAR                varchar(4)    not null, \
    REDUCTION_DIV_CD    varchar(2)    not null, \
    REDUCTION_DIV_NAME  varchar(45)   not null, \
    SCHOLARSHIP         varchar(2)    not null, \
    SEMESTER_DIV        varchar(1), \
    VALID_MONTH         varchar(2), \
    REDUCTION_DIV       varchar(1), \
    MONEY_DIV           varchar(1), \
    NUMERATOR           smallint, \
    DENOMINATOR         smallint, \
    MONEY               integer, \
    REDUCTION_TIMING    varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SCHOOL_MST add constraint PK_REDUC_SCHOOL primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_DIV_CD)