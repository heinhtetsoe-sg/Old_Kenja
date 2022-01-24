-- kanji=����
-- $Id: da3fa67a5b0d66af61fca5db305dce25ae60b105 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_INVEST_ATTENTION_DAT

create table HEALTH_INVEST_ATTENTION_DAT \
        (SCHREGNO               varchar(8)      not null, \
         ATTENTION              varchar(600), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_INVEST_ATTENTION_DAT add constraint pk_hea_inva_dat primary key \
        (SCHREGNO)
