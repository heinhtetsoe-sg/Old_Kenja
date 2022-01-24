-- kanji=����
-- $Id: 0605cbf98c00f005c1478eca3af789cbe76549f8 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table CLUB_ITEM_DAT_OLD
create table CLUB_ITEM_DAT_OLD like CLUB_ITEM_DAT
insert into CLUB_ITEM_DAT_OLD select * from CLUB_ITEM_DAT

drop table CLUB_ITEM_DAT

create table CLUB_ITEM_DAT \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    CLUBCD         VARCHAR (4) not null, \
    ITEMCD         VARCHAR (3) not null, \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_ITEM_DAT add constraint PK_CLUB_ITEM_DAT \
primary key (SCHOOLCD, SCHOOL_KIND, CLUBCD,ITEMCD)

insert into CLUB_ITEM_DAT \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        CLUBCD, \
        ITEMCD, \
        REGISTERCD, \
        UPDATED \
from CLUB_ITEM_DAT_OLD
