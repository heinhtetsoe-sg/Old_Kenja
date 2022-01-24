-- kanji=����
-- $Id: 67c2b0a41f2042307e229933a26ceb8bba7b3fe4 $
-- ���Һ��ҥǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--


drop table SCHREG_REGD_GDAT

create table SCHREG_REGD_GDAT \
      (YEAR         varchar(4) not null, \
       GRADE        varchar(2) not null, \
       SCHOOL_KIND  varchar(2) not null, \
       GRADE_CD     varchar(2) not null, \
       GRADE_NAME1  varchar(60) not null, \
       GRADE_NAME2  varchar(60), \
       GRADE_NAME3  varchar(60), \
       REGISTERCD   varchar(8), \
       UPDATED      timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table SCHREG_REGD_GDAT add constraint PK_SCHREG_REGD_G primary key (YEAR, GRADE)
