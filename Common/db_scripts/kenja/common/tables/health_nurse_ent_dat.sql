-- kanji=����
-- $Id: 6f306eb3c0c04223d239a571face9eeefe20a514 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_NURSE_ENT_DAT

create table HEALTH_NURSE_ENT_DAT \
        (SCHREGNO               varchar(8)      not null, \
         INSURED_NAME           varchar(60), \
         INSURED_MARK           varchar(60), \
         INSURED_NO             varchar(20), \
         INSURANCE_NAME         varchar(60), \
         INSURANCE_NO           varchar(20), \
         VALID_DATE             date, \
         AUTHORIZE_DATE         date, \
         RELATIONSHIP           varchar(2), \
         REMARK                 varchar(1200), \
         ATTENTION              varchar(90), \
         REGISTERCD             varchar(8), \
         UPDATED                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table HEALTH_NURSE_ENT_DAT add constraint pk_hea_nure_dat primary key \
        (SCHREGNO)
