-- $Id: reduction_country_dat.sql 69862 2019-09-25 02:45:17Z yamashiro $

drop table REDUCTION_COUNTRY_DAT
create table REDUCTION_COUNTRY_DAT( \
    YEAR                    varchar(4)    not null, \
    SCHREGNO                varchar(8)    not null, \
    GRADE                   varchar(2), \
    OFFSET_FLG              varchar(1), \
    REDUC_RARE_CASE_CD_1    varchar(2), \
    REDUCTION_SEQ_1         integer, \
    REDUCTIONMONEY_1        integer, \
    REDUC_DEC_FLG_1         varchar(1), \
    REDUCTION_ADD_MONEY_1   integer, \
    REDUC_ADD_FLG_1         varchar(1), \
    REDUC_INCOME_1          integer, \
    INCOME_SIBLINGS1        smallint, \
    REDUC_RARE_CASE_CD_2    varchar(2), \
    REDUCTION_SEQ_2         integer, \
    REDUCTIONMONEY_2        integer, \
    REDUC_DEC_FLG_2         varchar(1), \
    REDUCTION_ADD_MONEY_2   integer, \
    REDUC_ADD_FLG_2         varchar(1), \
    REDUC_INCOME_2          integer, \
    INCOME_SIBLINGS2        smallint, \
    REDUC_REMARK            varchar(30), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_COUNTRY_DAT add constraint PK_REDUCTION_C_DAT primary key(YEAR, SCHREGNO)
