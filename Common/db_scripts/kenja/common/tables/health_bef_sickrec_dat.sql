-- kanji=����
-- $Id: 4a862c7c4736d2e8f34e8b0adb3fea6f97c1a712 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_BEF_SICKREC_DAT

create table HEALTH_BEF_SICKREC_DAT \
        (SCHREGNO               varchar(8)      not null, \
         SEQ                    varchar(2)      not null, \
         DISEASE                varchar(60), \
         S_YEAR                 varchar(4), \
         S_MONTH                varchar(2), \
         E_YEAR                 varchar(4), \
         E_MONTH                varchar(2), \
         SITUATION              varchar(120), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_BEF_SICKREC_DAT add constraint pk_hea_bsic_dat primary key \
        (SCHREGNO, SEQ)
