-- $Id: 6caf67d9b619befe516abe4cc0f4c025ec4d5175 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table RELATIVEASSESS_RATE_MST

create table RELATIVEASSESS_RATE_MST( \
     YEAR           VARCHAR(4)   NOT NULL, \
     ASSESSLEVEL    SMALLINT     NOT NULL, \
     ASSESSRATE     SMALLINT     , \
     REGISTERCD     VARCHAR(10), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table RELATIVEASSESS_RATE_MST add constraint pk_relaas_rate_ms primary key \
    (YEAR, ASSESSLEVEL)
