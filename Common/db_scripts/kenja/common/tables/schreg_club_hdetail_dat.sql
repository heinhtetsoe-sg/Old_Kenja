-- kanji=����
-- $Id: 9f3d8690808a5e0bfdf88c24a830722e47192ecb $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table SCHREG_CLUB_HDETAIL_DAT

create table SCHREG_CLUB_HDETAIL_DAT \
( \
    SCHOOLCD            varchar(12) not null, \
    SCHOOL_KIND         varchar(2)  not null, \
    SCHREGNO            varchar(8)  not null, \
    CLUBCD              varchar(4)  not null, \
    DETAIL_DATE         date        not null, \
    DETAIL_SEQ          integer     not null, \
    MEET_NAME           varchar(90), \
    DIV                 varchar(1) not null, \
    GROUPCD             varchar(5), \
    HOSTCD              varchar(2), \
    ITEMCD              varchar(3), \
    KINDCD              varchar(3), \
    RECORDCD            varchar(3), \
    DOCUMENT            varchar(60), \
    DETAIL_REMARK       varchar(60), \
    DETAIL_SCHOOL_KIND  varchar(2), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_CLUB_HDETAIL_DAT add constraint PK_SCH_CLUB_HD_DAT \
primary key (SCHOOLCD, SCHOOL_KIND, SCHREGNO,CLUBCD,DETAIL_DATE,DETAIL_SEQ)
