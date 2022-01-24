-- kanji=����
-- $Id: a8a3cc1696d70a9495711a7f984871ed141b5ba4 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table MOCK_DISP_GROUP_MST

create table MOCK_DISP_GROUP_MST \
    (YEAR          varchar(4) not null, \
     GROUPCD       varchar(4) not null, \
     GROUPNAME     varchar(60), \
     GROUPABBV     varchar(30), \
     REGISTERCD    varchar(10), \
     UPDATED       timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_DISP_GROUP_MST add constraint PK_MOCK_DISP_GROUP_MST primary key (YEAR, GROUPCD)


