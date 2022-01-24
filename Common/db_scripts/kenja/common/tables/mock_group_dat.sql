-- kanji=����
-- $Id: 528833f06f0c9b3788e465a1af22bb84a66678da $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table MOCK_GROUP_DAT

create table MOCK_GROUP_DAT \
    (YEAR           varchar(4) not null, \
     GROUP_DIV      varchar(1) not null, \
     TARGET_DIV     varchar(1) not null, \
     STF_AUTH_CD    varchar(8) not null, \
     GROUPCD        varchar(4) not null, \
     MOCK_TARGET_CD varchar(9) not null, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_GROUP_DAT add constraint pk_mock_group_dat primary key (YEAR, GROUP_DIV, TARGET_DIV, STF_AUTH_CD, GROUPCD, MOCK_TARGET_CD)


