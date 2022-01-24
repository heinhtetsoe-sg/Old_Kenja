-- kanji=����
-- $Id: sales_tightens_hist_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table SALES_TIGHTENS_HIST_DAT

create table SALES_TIGHTENS_HIST_DAT \
    (SALES_YEAR_MONTH    varchar(6) not null, \
     YEAR                varchar(4), \
     S_TIGHTENS_DATE     date, \
     E_TIGHTENS_DATE     date, \
     TEMP_TIGHTENS_FLAG  varchar(1), \
     REGISTERCD          varchar(8)  , \
     UPDATED             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table SALES_TIGHTENS_HIST_DAT add constraint PK_SALES_TIGHTENS primary key \
      (SALES_YEAR_MONTH)
