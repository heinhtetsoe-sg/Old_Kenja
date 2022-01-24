-- $Id: fb81e972a80d0de1f42e7da86f709f9713421cf7 $

drop TABLE REDUCTION_DAT
create table REDUCTION_DAT( \
    SCHOOLCD             varchar(12) not null, \
    SCHOOL_KIND          varchar(2)  not null, \
    YEAR                 varchar(4)  not null, \
    REDUCTION_TARGET     varchar(1)  not null, \
    SLIP_NO              varchar(15) not null, \
    SCHREGNO             varchar(8)  not null, \
    PREFECTURESCD        varchar(2), \
    GRADE                varchar(2), \
    OFFSET_FLG           varchar(1), \
    RARE_CASE_CD_1       varchar(2), \
    LOCK_FLG             varchar(1), \
    SEQ_1                integer, \
    MONEY_1              integer, \
    DEC_FLG_1            varchar(1), \
    INCOME_1             integer, \
    RANK_1               varchar(2), \
    SEQ_2                integer, \
    MONEY_2              integer, \
    DEC_FLG_2            varchar(1), \
    INCOME_2             integer, \
    RANK_2               varchar(2), \
    RARE_CASE_CD_2       varchar(2), \
    REMARK               varchar(30), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_DAT add constraint PK_REDUCTION_DAT primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_TARGET, SLIP_NO)
