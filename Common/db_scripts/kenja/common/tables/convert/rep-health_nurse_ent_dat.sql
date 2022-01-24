-- kanji=����
-- $Id: 601e0b5882379de101f6ee444bfa2158d7a291a2 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table HEALTH_NURSE_ENT_DAT_OLD

create table HEALTH_NURSE_ENT_DAT_OLD like HEALTH_NURSE_ENT_DAT

insert into HEALTH_NURSE_ENT_DAT_OLD select * from HEALTH_NURSE_ENT_DAT

drop table HEALTH_NURSE_ENT_DAT

create table HEALTH_NURSE_ENT_DAT ( \
         SCHREGNO               varchar(8)      not null, \
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

insert into HEALTH_NURSE_ENT_DAT \
  select \
    SCHREGNO, \
    INSURED_NAME, \
    INSURED_MARK, \
    INSURED_NO, \
    INSURANCE_NAME, \
    INSURANCE_NO, \
    VALID_DATE, \
    AUTHORIZE_DATE, \
    RELATIONSHIP, \
    REMARK, \
    ATTENTION, \
    REGISTERCD, \
    UPDATED \
  FROM \
    HEALTH_NURSE_ENT_DAT_OLD T1 \

alter table HEALTH_NURSE_ENT_DAT add constraint pk_hea_nure_dat primary key (SCHREGNO)
