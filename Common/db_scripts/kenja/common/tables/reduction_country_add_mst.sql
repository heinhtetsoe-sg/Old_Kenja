-- $Id: c1f71f4cca36cc1dfc53b3f9195e7304965a2a31 $

drop table REDUCTION_COUNTRY_ADD_MST

create table REDUCTION_COUNTRY_ADD_MST( \
    SCHOOLCD             varchar(12)   not null, \
    SCHOOL_KIND          varchar(2)    not null, \
    YEAR                 varchar(4)    not null, \
    SCHOOLDIV            varchar(1)    not null, \
    GRADE                varchar(2)    not null, \
    REDUCTION_SEQ        integer       not null generated always as identity (start with 1, increment by 1 ,minvalue 1, no maxvalue, no cycle, no cache, order), \
    REDUCTION_ADD_MONEY1 integer, \
    INCOME_LOW1          integer, \
    INCOME_HIGH1         integer, \
    INCOME_RANK1         varchar(2), \
    REDUCTION_ADD_MONEY2 integer, \
    INCOME_LOW2          integer, \
    INCOME_HIGH2         integer, \
    INCOME_RANK2         varchar(2), \
    REDUCTIONREMARK      varchar(75), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_COUNTRY_ADD_MST add constraint PK_REDUCTION_CA_M primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHOOLDIV, GRADE, REDUCTION_SEQ)
