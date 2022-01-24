-- kanji=����
-- $Id: 918167ba5cea1ec0e4aebb52f6a5ac60a58d7f2e $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table MOCK_TARGET_DAT_OLD
create table MOCK_TARGET_DAT_OLD like MOCK_TARGET_DAT
insert into MOCK_TARGET_DAT_OLD select * from MOCK_TARGET_DAT

drop table MOCK_TARGET_DAT
create table MOCK_TARGET_DAT \
    (TARGET_DIV         varchar(1) not null, \
     STF_AUTH_CD        varchar(8) not null, \
     TARGETCD           varchar(9) not null, \
     MOCK_SUBCLASS_CD   varchar(6) not null, \
     SUBCLASS_NAMECD    varchar(10), \
     SUBCLASS_NAME      varchar(30), \
     POINT_CONVERSION   smallint, \
     SCORE              smallint, \
     DEVIATION          decimal (4,1), \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table MOCK_TARGET_DAT add constraint pk_mock_target_dat primary key (TARGET_DIV, STF_AUTH_CD, TARGETCD, MOCK_SUBCLASS_CD)

insert into MOCK_TARGET_DAT \
select \
    TARGET_DIV, \
    STF_AUTH_CD, \
    TARGETCD, \
    right(rtrim('000000'||SUBCLASS_DIV),6) as MOCK_SUBCLASS_CD, \
    SUBCLASS_NAMECD, \
    SUBCLASS_NAME, \
    POINT_CONVERSION, \
    SCORE, \
    DEVIATION, \
    REGISTERCD, \
    UPDATED \
from \
    MOCK_TARGET_DAT_OLD

