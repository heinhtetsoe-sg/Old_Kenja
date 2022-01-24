-- kanji=����
-- $Id: 92a4647d79a2acecb3c7044efe81d30b99447641 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table MOCK_DISP_GROUP_DAT

create table MOCK_DISP_GROUP_DAT \
    (YEAR           varchar(4) not null, \
     GROUPCD        varchar(4) not null, \
     MOCKCD         varchar(9) not null, \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_DISP_GROUP_DAT add constraint PK_MOCK_DISP_GROUP_DAT primary key (YEAR, GROUPCD, MOCKCD)


