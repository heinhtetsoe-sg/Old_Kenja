-- kanji=����
-- $Id: 8b1fc554efcb89074ebbfb4258df20d4bdec4b26 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop table FACILITY_GATE_DAT

create table FACILITY_GATE_DAT ( \
     FACCD          varchar(4) not null, \
     GATENO         varchar(4) not null, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table FACILITY_GATE_DAT add constraint PK_FAC_GATE_DAT primary key (FACCD, GATENO)

