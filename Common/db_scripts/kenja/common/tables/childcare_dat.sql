-- kanji=����
-- $Id: afcdf0322075d16e0e2c04944e09f1e406a66042 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table CHILDCARE_DAT

create table CHILDCARE_DAT \
    (YEAR                   varchar(4) not null, \
     SCHREGNO               varchar(8) not null, \
     CARE_DATE              date not null, \
     FARE_CD                varchar(2), \
     PICK_UP                varchar(300), \
     REMARK                 varchar(300), \
     EXTRACURRICULAR_CLASS  varchar(300), \
     REGISTERCD             varchar(10), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table CHILDCARE_DAT add constraint PK_CHILDCARE_DAT primary key (YEAR, SCHREGNO, CARE_DATE)


