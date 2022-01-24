-- $Id: 90502c6dc67a5bfd99322d9a42cf5705eb8e0ad4 $

drop table REDUCTION_SCHOOL_LM_MST

create table REDUCTION_SCHOOL_LM_MST( \
    SCHOOLCD            varchar(12)   not null, \
    SCHOOL_KIND         varchar(2)    not null, \
    YEAR                varchar(4)    not null, \
    REDUCTION_DIV_CD    varchar(2)    not null, \
    COLLECT_L_CD        varchar(2)    not null, \
    COLLECT_M_CD        varchar(2)    not null, \
    MONEY_DIV           varchar(1), \
    NUMERATOR           smallint, \
    DENOMINATOR         smallint, \
    MONEY               integer, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SCHOOL_LM_MST add constraint PK_REDUC_SCHOOL_LM primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_DIV_CD, COLLECT_L_CD, COLLECT_M_CD)