-- kanji=����
-- $Id: dd30636dffaab880b22fa503da0f62f767f58cb2 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_RELA_DAT

create table HEALTH_RELA_DAT \
        (SCHREGNO               varchar(8)      not null, \
         RELANO                 varchar(2)      not null, \
         REMARK                 varchar(90), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_RELA_DAT add constraint pk_hea_rela_dat primary key \
        (SCHREGNO, RELANO)
