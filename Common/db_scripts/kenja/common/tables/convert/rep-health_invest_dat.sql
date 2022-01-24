-- kanji=����
-- $Id: e7208aedb0461675113591e8ebcf486bf8e0aae4 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_INVEST_DAT_OLD

create table HEALTH_INVEST_DAT_OLD like HEALTH_INVEST_DAT

insert into HEALTH_INVEST_DAT_OLD select * from HEALTH_INVEST_DAT

drop table HEALTH_INVEST_DAT

create table HEALTH_INVEST_DAT ( \
         SCHREGNO               varchar(8)      not null, \
         YEAR                   varchar(4)      not null, \
         E_YEAR                 varchar(4), \
         E_MONTH                varchar(2), \
         QUESTIONCD             varchar(2)      not null, \
         ANSWER                 varchar(1), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

insert into HEALTH_INVEST_DAT \
  select \
    SCHREGNO, \
    YEAR, \
    E_YEAR, \
    E_MONTH, \
    value(QUESTIONCD,'00') as QUESTIONCD, \
    ANSWER, \
    REGISTERCD, \
    UPDATED \
  FROM \
    HEALTH_INVEST_DAT_OLD T1 \

alter table HEALTH_INVEST_DAT add constraint pk_hea_inv_dat primary key (SCHREGNO, YEAR, QUESTIONCD)
