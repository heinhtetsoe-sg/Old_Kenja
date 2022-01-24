-- kanji=����
-- $Id: 1b3c8fb1c237e98c80355e0a7328bc78aee3f0cb $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table CLUB_HOST_MST

create table CLUB_HOST_MST \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    HOSTCD         VARCHAR (2) not null, \
    HOSTNAME       VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table CLUB_HOST_MST add constraint PK_CLUB_HOST_MST \
primary key (SCHOOLCD, SCHOOL_KIND, HOSTCD)
