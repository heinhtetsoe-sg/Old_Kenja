-- kanji=����
-- $Id: 91bde4d997852203712f5ca57e373491a3d24d75 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table SCHREG_CLUB_HDETAIL_CSV_DAT

create table SCHREG_CLUB_HDETAIL_CSV_DAT \
( \
    SCHOOLCD       VARCHAR (12) not null, \
    SCHOOL_KIND    VARCHAR (2) not null, \
    SCHREGNO       VARCHAR (8) not null, \
    CLUBCD         VARCHAR (4) not null, \
    DETAIL_DATE    DATE        not null, \
    DETAIL_SEQ     INTEGER     not null, \
    MEET_NAME      VARCHAR (90), \
    DIV            VARCHAR (1) not null, \
    GROUPCD        VARCHAR (5), \
    HOSTCD         VARCHAR (2), \
    ITEMCD         VARCHAR (3), \
    KINDCD         VARCHAR (3), \
    RECORDCD       VARCHAR (3), \
    DOCUMENT       VARCHAR (60), \
    DETAIL_REMARK  VARCHAR (60), \
    REGISTERCD     VARCHAR (10), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_CLUB_HDETAIL_CSV_DAT add constraint PK_SCH_CLUB_HD_CSV \
primary key (SCHOOLCD, SCHOOL_KIND, SCHREGNO,CLUBCD,DETAIL_DATE,DETAIL_SEQ)
