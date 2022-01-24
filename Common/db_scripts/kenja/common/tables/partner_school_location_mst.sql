-- $Id: ee717304d463c4b96e4d45fefec9837c762ac152 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table PARTNER_SCHOOL_LOCATION_MST

create table PARTNER_SCHOOL_LOCATION_MST( \
     DISTRICTCD          VARCHAR(5)     NOT NULL, \
     DISTRICT_NAME       VARCHAR(75)    NOT NULL, \
     DISTRICT_NAME_ABBV  VARCHAR(75), \
     REGISTERCD          VARCHAR(10), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table PARTNER_SCHOOL_LOCATION_MST add constraint pk_prtnsch_locat_m primary key \
    (DISTRICTCD)
