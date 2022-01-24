-- $Id: rep-reduction_mst.sql 69851 2019-09-24 09:27:09Z yamashiro $

drop table REDUCTION_MST_OLD
create table REDUCTION_MST_OLD like REDUCTION_MST
insert into REDUCTION_MST_OLD select * from REDUCTION_MST

drop table REDUCTION_MST
create table REDUCTION_MST( \
    YEAR              varchar(4)    not null, \
    PREFECTURESCD     varchar(2)    not null, \
    GRADE             varchar(2)    not null, \
    REDUCTION_SEQ     integer       not null generated always as identity (START WITH 1, INCREMENT BY 1 ,MINVALUE 1, NO MAXVALUE, NO CYCLE, NO CACHE, ORDER), \
    INCOME_LOW1       integer, \
    INCOME_HIGH1      integer, \
    INCOME_SIBLINGS1  smallint, \
    INCOME_RANK1      varchar(2), \
    REDUCTIONMONEY_1  integer, \
    INCOME_LOW2       integer, \
    INCOME_HIGH2      integer, \
    INCOME_SIBLINGS2  smallint, \
    INCOME_RANK2      varchar(2), \
    REDUCTIONMONEY_2  integer, \
    REDUCTIONREMARK   varchar(75), \
    REGISTERCD        varchar(8), \
    UPDATED           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REDUCTION_MST add constraint PK_REDUCTION_MST primary key (YEAR,PREFECTURESCD,GRADE,REDUCTION_SEQ)

insert into reduction_mst \
    (YEAR, \
     PREFECTURESCD, \
     GRADE, \
     INCOME_LOW1, \
     INCOME_HIGH1, \
     INCOME_SIBLINGS1, \
     INCOME_RANK1, \
     REDUCTIONMONEY_1, \
     INCOME_LOW2, \
     INCOME_HIGH2, \
     INCOME_SIBLINGS2, \
     INCOME_RANK2, \
     REDUCTIONMONEY_2, \
     REDUCTIONREMARK, \
     REGISTERCD, \
     UPDATED \
    ) \
    SELECT \
        YEAR, \
        PREFECTURESCD, \
        GRADE, \
        INCOME_LOW1, \
        INCOME_HIGH1, \
        cast(null as smallint) as INCOME_SIBLINGS1, \
        INCOME_RANK1, \
        REDUCTIONMONEY_1, \
        INCOME_LOW2, \
        INCOME_HIGH2, \
        cast(null as smallint) as INCOME_SIBLINGS2, \
        INCOME_RANK2, \
        REDUCTIONMONEY_2, \
        REDUCTIONREMARK, \
        REGISTERCD, \
        UPDATED \
    FROM \
        REDUCTION_MST_OLD
