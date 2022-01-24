-- $Id: 9d01c340052e5f07b875364510ed74de7fc8efb2 $

drop table REDUCTION_COUNTRY_MST_OLD

create table REDUCTION_COUNTRY_MST_OLD like REDUCTION_COUNTRY_MST

insert into REDUCTION_COUNTRY_MST_OLD select * from REDUCTION_COUNTRY_MST

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

INSERT INTO REDUCTION_COUNTRY_MST \
( \
SCHOOLCD, \
SCHOOL_KIND, \
YEAR, \
SCHOOLDIV, \
GRADE, \
REDUCTIONMONEY1, \
REDUCTION_ADD_MONEY1, \
INCOME_LOW1, \
INCOME_HIGH1, \
INCOME_RANK1, \
REDUCTIONMONEY2, \
REDUCTION_ADD_MONEY2, \
INCOME_LOW2, \
INCOME_HIGH2, \
INCOME_RANK2, \
REDUCTIONREMARK, \
REGISTERCD, \
UPDATED \
) \
SELECT \
    T1.SCHOOLCD, \
    T1.SCHOOL_KIND, \
    T1.YEAR, \
    T1.SCHOOLDIV, \
    T1.GRADE, \
    T1.REDUCTIONMONEY1, \
    T2.REDUCTION_ADD_MONEY1, \
    T1.INCOME_LOW1, \
    T1.INCOME_HIGH1, \
    T1.INCOME_RANK1, \
    T1.REDUCTIONMONEY2, \
    T2.REDUCTION_ADD_MONEY2, \
    T1.INCOME_LOW2, \
    T1.INCOME_HIGH2, \
    T1.INCOME_RANK2, \
    T1.REDUCTIONREMARK, \
    T1.REGISTERCD, \
    T1.UPDATED \
FROM REDUCTION_COUNTRY_MST_OLD T1 \
    LEFT JOIN REDUCTION_COUNTRY_ADD_MST T2 ON T1.SCHOOLCD     = T2.SCHOOLCD \
                                          AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND \
                                          AND T1.YEAR         = T2.YEAR \
                                          AND T1.SCHOOLDIV    = T2.SCHOOLDIV \
                                          AND T1.GRADE        = T2.GRADE \
                                          AND T1.INCOME_LOW1  = T2.INCOME_LOW1 \
                                          AND T1.INCOME_HIGH1 = T2.INCOME_HIGH1 \
                                          AND T1.INCOME_LOW2  = T2.INCOME_LOW2 \
                                          AND T1.INCOME_HIGH2 = T2.INCOME_HIGH2
