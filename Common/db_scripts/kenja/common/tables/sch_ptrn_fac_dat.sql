-- kanji=����
-- $Id: 5e2f4e953d8967f0b2bf4574fc25c8a2cb4415e2 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table SCH_PTRN_FAC_DAT

create table SCH_PTRN_FAC_DAT \
      (YEAR               varchar(4)   not null, \
       SEMESTER           varchar(1)   not null, \
       BSCSEQ             smallint     not null, \
       DAYCD              varchar(1)   not null, \
       PERIODCD           varchar(1)   not null, \
       CHAIRCD            varchar(7)   not null, \
       FACCD              varchar(4)   not null, \
       REGISTERCD         varchar(8), \
       UPDATED            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCH_PTRN_FAC_DAT add constraint pk_sch_ptrn_fac \
      primary key (YEAR, SEMESTER, BSCSEQ, DAYCD, PERIODCD, CHAIRCD, FACCD)
