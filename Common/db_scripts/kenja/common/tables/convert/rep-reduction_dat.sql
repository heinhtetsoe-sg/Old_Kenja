-- $Id: be38aea15e567406976e9df305c415dc28f09edf $

drop table REDUCTION_DAT_OLD

create table REDUCTION_DAT_OLD like REDUCTION_DAT

insert into REDUCTION_DAT_OLD select * from REDUCTION_DAT

drop table REDUCTION_DAT

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
    INCOME_1_1           integer, \
    INCOME_1_2           integer, \
    INCOME_1_3           integer, \
    INCOME_1_4           integer, \
    RANK_1               varchar(2), \
    SEQ_2                integer, \
    MONEY_2              integer, \
    DEC_FLG_2            varchar(1), \
    INCOME_2             integer, \
    INCOME_2_1           integer, \
    INCOME_2_2           integer, \
    INCOME_2_3           integer, \
    INCOME_2_4           integer, \
    RANK_2               varchar(2), \
    RARE_CASE_CD_2       varchar(2), \
    REMARK               varchar(30), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_DAT add constraint PK_REDUCTION_DAT primary key (SCHOOLCD, SCHOOL_KIND, YEAR, REDUCTION_TARGET, SLIP_NO)


insert into REDUCTION_DAT \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    '1' AS REDUCTION_TARGET, \
    SLIP_NO, \
    SCHREGNO, \
    PREFECTURESCD, \
    GRADE, \
    OFFSET_FLG, \
    RARE_CASE_CD_1, \
    LOCK_FLG, \
    SEQ_1, \
    MONEY_1, \
    DEC_FLG_1, \
    INCOME_1, \
    INCOME_1_1, \
    INCOME_1_2, \
    INCOME_1_3, \
    INCOME_1_4, \
    RANK_1, \
    SEQ_2, \
    MONEY_2, \
    DEC_FLG_2, \
    INCOME_2, \
    INCOME_2_1, \
    INCOME_2_2, \
    INCOME_2_3, \
    INCOME_2_4, \
    RANK_2, \
    RARE_CASE_CD_2, \
    REMARK, \
    REGISTERCD, \
    UPDATED \
FROM REDUCTION_DAT_OLD
