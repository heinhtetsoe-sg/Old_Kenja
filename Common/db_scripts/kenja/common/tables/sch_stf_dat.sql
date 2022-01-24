-- kanji=����
-- $Id: d9d9f4c27e1eeaf263cf60a0e992150cd1d013d6 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table SCH_STF_DAT

create table SCH_STF_DAT \
      (EXECUTEDATE        date         not null, \
       PERIODCD           varchar(1)   not null, \
       CHAIRCD            varchar(7)   not null, \
       STAFFCD            varchar(8)   not null, \
       REGISTERCD         varchar(8), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCH_STF_DAT add constraint pk_sch_stf_dat \
      primary key (EXECUTEDATE, PERIODCD, CHAIRCD, STAFFCD)
