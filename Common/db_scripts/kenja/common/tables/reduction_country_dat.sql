-- $Id: c51d9a381bda5d4d63bf1355dce00045ab262193 $

drop table REDUCTION_COUNTRY_DAT
create table REDUCTION_COUNTRY_DAT( \
    SCHOOLCD                varchar(12) not null, \
    SCHOOL_KIND             varchar(2)  not null, \
    YEAR                    varchar(4)  not null, \
    SLIP_NO                 varchar(15) not null, \
    SCHREGNO                varchar(8)  not null, \
    GRADE                   varchar(2), \
    OFFSET_FLG              varchar(1), \
    RARE_CASE_CD_1          varchar(2), \
    SEQ_1                   integer, \
    RANK_1                  varchar(2), \
    MONEY_1                 integer, \
    DEC_FLG_1               varchar(1), \
    ADD_MONEY_1             integer, \
    ADD_DEC_FLG_1           varchar(1), \
    INCOME_1                integer, \
    INCOME_1_1              integer, \
    INCOME_1_2              integer, \
    INCOME_1_3              integer, \
    INCOME_1_4              integer, \
    RARE_CASE_CD_2          varchar(2), \
    SEQ_2                   integer, \
    RANK_2                  varchar(2), \
    MONEY_2                 integer, \
    DEC_FLG_2               varchar(1), \
    ADD_MONEY_2             integer, \
    ADD_DEC_FLG_2           varchar(1), \
    INCOME_2                integer, \
    INCOME_2_1              integer, \
    INCOME_2_2              integer, \
    INCOME_2_3              integer, \
    INCOME_2_4              integer, \
    REDUCTION_JUGYOURYOU    integer, \
    REDUCTION_JUGYOURYOUTOU integer, \
    REDUCTION_NYUGAKUKIN    integer, \
    REMARK                  varchar(30), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_COUNTRY_DAT add constraint PK_REDUCTION_C_DAT primary key(SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO)

