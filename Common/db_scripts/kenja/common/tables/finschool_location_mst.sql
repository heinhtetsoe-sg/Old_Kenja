-- $Id: 291e957fa645b164acc615715901a072ffbf882c $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table FINSCHOOL_LOCATION_MST

create table FINSCHOOL_LOCATION_MST( \
     DISTRICTCD          VARCHAR(5)     NOT NULL, \
     DISTRICT_NAME       VARCHAR(75)    NOT NULL, \
     DISTRICT_NAME_ABBV  VARCHAR(75), \
     REGISTERCD          VARCHAR(10), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table FINSCHOOL_LOCATION_MST add constraint pk_finsch_locat_m primary key \
    (DISTRICTCD)
