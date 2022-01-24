-- kanji=����
-- $Id: collect_grp_schreg_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--���ܥ��롼��HR���饹�ǡ���
drop table COLLECT_GRP_SCHREG_DAT

create table COLLECT_GRP_SCHREG_DAT \
( \
        "YEAR"            varchar(4) not null, \
        "COLLECT_GRP_CD"  varchar(4) not null, \
        "SCHREGNO"        varchar(8) not null, \
        "REGISTERCD"      varchar(10), \
        "UPDATED"         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_GRP_SCHREG_DAT add constraint PK_COL_GRP_SCH_DAT primary key (YEAR, COLLECT_GRP_CD, SCHREGNO)
