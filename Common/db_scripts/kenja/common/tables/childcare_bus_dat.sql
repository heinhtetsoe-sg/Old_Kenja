-- kanji=����
-- $Id: b31d8b6f9b028b2044c6ea1261be490e6dab1685 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table CHILDCARE_BUS_DAT

create table CHILDCARE_BUS_DAT \
    (YEAR           varchar(4) not null, \
     SCHREGNO       varchar(8) not null, \
     CARE_DATE      DATE, \
     SCHEDULE_CD    varchar(1), \
     COURSE_CD      varchar(2), \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CHILDCARE_BUS_DAT add constraint PK_CHILDCARE_BUS primary key (YEAR, SCHREGNO)


