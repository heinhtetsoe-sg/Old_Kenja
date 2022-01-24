-- kanji=����
-- $Id: 50c5df115d9238ae4387e38cc78c0fe4b11b8fca $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_INVEST_DAT

create table HEALTH_INVEST_DAT \
        (SCHREGNO               varchar(8)      not null, \
         YEAR                   varchar(4)      not null, \
         E_YEAR                 varchar(4), \
         E_MONTH                varchar(2), \
         QUESTIONCD             varchar(2)      not null, \
         ANSWER                 varchar(1), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_INVEST_DAT add constraint pk_hea_inv_dat primary key \
        (SCHREGNO, YEAR, QUESTIONCD)
