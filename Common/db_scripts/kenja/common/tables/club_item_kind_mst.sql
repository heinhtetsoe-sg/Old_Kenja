-- kanji=����
-- $Id: e16d068ae7af18e275bdc13ca5790aa721fd4e8e $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table CLUB_ITEM_KIND_MST

create table CLUB_ITEM_KIND_MST \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    ITEMCD         VARCHAR (3) not null, \
    KINDCD         VARCHAR (3) not null, \
    KINDNAME       VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_ITEM_KIND_MST add constraint PK_CLB_ITM_KND_MST \
primary key (SCHOOLCD, SCHOOL_KIND, ITEMCD, KINDCD)
