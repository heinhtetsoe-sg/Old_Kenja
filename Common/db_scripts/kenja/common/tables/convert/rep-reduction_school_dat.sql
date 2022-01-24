-- $Id: e6801faa2a15165de8557daf717a0c8aca0daeec $

drop table REDUCTION_SCHOOL_DAT_OLD

create table REDUCTION_SCHOOL_DAT_OLD like REDUCTION_SCHOOL_DAT

insert into REDUCTION_SCHOOL_DAT_OLD select * from REDUCTION_SCHOOL_DAT

drop TABLE REDUCTION_SCHOOL_DAT
create table REDUCTION_SCHOOL_DAT( \
    SCHOOLCD             varchar(12) not null, \
    SCHOOL_KIND          varchar(2)  not null, \
    YEAR                 varchar(4)  not null, \
    SLIP_NO              varchar(15) not null, \
    REDUCTION_TARGET     varchar(1)  not null, \
    SCHREGNO             varchar(8)  not null, \
    OFFSET_FLG           varchar(1), \
    LOCK_FLG             varchar(1), \
    MONEY_1              integer, \
    DEC_FLG_1            varchar(1), \
    MONEY_2              integer, \
    DEC_FLG_2            varchar(1), \
    REMARK               varchar(30), \
    REGISTERCD           varchar(10), \
    UPDATED              timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_SCHOOL_DAT add constraint PK_REDUC_SCHOOL_D primary key (SCHOOLCD, SCHOOL_KIND, YEAR, SLIP_NO, REDUCTION_TARGET)

INSERT INTO REDUCTION_SCHOOL_DAT \
SELECT \
    SCHOOLCD, \
    SCHOOL_KIND, \
    YEAR, \
    SLIP_NO, \
    REDUCTION_DIV, \
    SCHREGNO, \
    OFFSET_FLG, \
    LOCK_FLG, \
    MONEY_1, \
    DEC_FLG_1, \
    MONEY_2, \
    DEC_FLG_2, \
    REMARK, \
    REGISTERCD, \
    UPDATED \
FROM REDUCTION_SCHOOL_DAT_OLD
