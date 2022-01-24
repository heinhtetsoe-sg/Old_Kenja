-- $Id: reduction_country_mst.sql 61920 2018-08-22 02:01:51Z yamashiro $

drop table REDUCTION_COUNTRY_MST

create table REDUCTION_COUNTRY_MST( \
    SCHOOLCD                varchar(12)   not null, \
    SCHOOL_KIND             varchar(2)    not null, \
    YEAR                    varchar(4)    not null, \
    SCHOOLDIV               varchar(1)    not null, \
    GRADE                   varchar(2)    not null, \
    REDUCTION_SEQ           integer       not null generated always as identity (start with 1, increment by 1 ,minvalue 1, no maxvalue, no cycle, no cache, order), \
    REDUCTIONMONEY1         integer, \
    REDUCTION_ADD_MONEY1    integer, \
    INCOME_LOW1             integer, \
    INCOME_HIGH1            integer, \
    INCOME_RANK1            varchar(2), \
    REDUCTIONMONEY2         integer, \
    REDUCTION_ADD_MONEY2    integer, \
    INCOME_LOW2             integer, \
    INCOME_HIGH2            integer, \
    INCOME_RANK2            varchar(2), \
    REDUCTIONREMARK         varchar(75), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_COUNTRY_MST add constraint PK_REDUCTION_C_MST primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SCHOOLDIV, GRADE, REDUCTION_SEQ)
