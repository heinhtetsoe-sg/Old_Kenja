-- $Id: a8ee3f9728021c5c75b2daeb6c7c836a75d9ab51 $

drop table REDUCTION_MST_OLD
create table REDUCTION_MST_OLD like REDUCTION_MST
insert into  REDUCTION_MST_OLD select * from REDUCTION_MST

drop table REDUCTION_MST
create table REDUCTION_MST( \
    SCHOOLCD              varchar(12)   not null, \
    SCHOOL_KIND           varchar(2)    not null, \
    YEAR                  varchar(4)    not null, \
    REDUCTION_TARGET      varchar(1)    not null, \
    PREFECTURESCD         varchar(2)    not null, \
    GRADE                 varchar(2)    not null, \
    REDUCTION_SEQ         integer       not null generated always as identity (start with 1, increment by 1 ,minvalue 1, no maxvalue, no cycle, no cache, order), \
    MONEY_DIV1            varchar(1), \
    NUMERATOR1            smallint, \
    DENOMINATOR1          smallint, \
    REDUCTIONMONEY_1      integer, \
    INCOME_LOW1           integer, \
    INCOME_HIGH1          integer, \
    INCOME_RANK1          varchar(2), \
    BURDEN_CHARGE_FLG1    varchar(1), \
    MONEY_DIV2            varchar(1), \
    NUMERATOR2            smallint, \
    DENOMINATOR2          smallint, \
    REDUCTIONMONEY_2      integer, \
    INCOME_LOW2           integer, \
    INCOME_HIGH2          integer, \
    INCOME_RANK2          varchar(2), \
    BURDEN_CHARGE_FLG2    varchar(1), \
    MONTH_MONEY_4         integer, \
    MONTH_MONEY_5         integer, \
    MONTH_MONEY_6         integer, \
    MONTH_MONEY_7         integer, \
    MONTH_MONEY_8         integer, \
    MONTH_MONEY_9         integer, \
    MONTH_MONEY_10        integer, \
    MONTH_MONEY_11        integer, \
    MONTH_MONEY_12        integer, \
    MONTH_MONEY_1         integer, \
    MONTH_MONEY_2         integer, \
    MONTH_MONEY_3         integer, \
    REDUCTIONREMARK       varchar(75), \
    REGISTERCD            varchar(10), \
    UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_MST add constraint PK_REDUCTION_MST primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_TARGET, PREFECTURESCD, GRADE, REDUCTION_SEQ)

insert into REDUCTION_MST \
        (SCHOOLCD, \
        SCHOOL_KIND, \
        YEAR, \
        REDUCTION_TARGET, \
        PREFECTURESCD, \
        GRADE, \
        MONEY_DIV1, \
        NUMERATOR1, \
        DENOMINATOR1, \
        REDUCTIONMONEY_1, \
        INCOME_LOW1, \
        INCOME_HIGH1, \
        INCOME_RANK1, \
        BURDEN_CHARGE_FLG1, \
        MONEY_DIV2, \
        NUMERATOR2, \
        DENOMINATOR2, \
        REDUCTIONMONEY_2, \
        INCOME_LOW2, \
        INCOME_HIGH2, \
        INCOME_RANK2, \
        BURDEN_CHARGE_FLG2, \
        MONTH_MONEY_4, \
        MONTH_MONEY_5, \
        MONTH_MONEY_6, \
        MONTH_MONEY_7, \
        MONTH_MONEY_8, \
        MONTH_MONEY_9, \
        MONTH_MONEY_10, \
        MONTH_MONEY_11, \
        MONTH_MONEY_12, \
        MONTH_MONEY_1, \
        MONTH_MONEY_2, \
        MONTH_MONEY_3, \
        REDUCTIONREMARK, \
        REGISTERCD, \
        UPDATED) \
    SELECT \
        SCHOOLCD, \
        SCHOOL_KIND, \
        YEAR, \
        REDUCTION_TARGET, \
        PREFECTURESCD, \
        GRADE, \
        MONEY_DIV1, \
        NUMERATOR1, \
        DENOMINATOR1, \
        REDUCTIONMONEY_1, \
        INCOME_LOW1, \
        INCOME_HIGH1, \
        INCOME_RANK1, \
        CAST(NULL AS varchar(1)) AS BURDEN_CHARGE_FLG1, \
        MONEY_DIV2, \
        NUMERATOR2, \
        DENOMINATOR2, \
        REDUCTIONMONEY_2, \
        INCOME_LOW2, \
        INCOME_HIGH2, \
        INCOME_RANK2, \
        CAST(NULL AS varchar(1)) AS BURDEN_CHARGE_FLG2, \
        MONTH_MONEY_4, \
        MONTH_MONEY_5, \
        MONTH_MONEY_6, \
        MONTH_MONEY_7, \
        MONTH_MONEY_8, \
        MONTH_MONEY_9, \
        MONTH_MONEY_10, \
        MONTH_MONEY_11, \
        MONTH_MONEY_12, \
        MONTH_MONEY_1, \
        MONTH_MONEY_2, \
        MONTH_MONEY_3, \
        REDUCTIONREMARK, \
        REGISTERCD, \
        UPDATED \
    FROM \
        REDUCTION_MST_OLD

