-- kanji=����
-- $Id: 0d8797f44c0600fd79b98a56db53a61c1e602159 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table PARTNER_SCHOOL_DETAIL_MST

create table PARTNER_SCHOOL_DETAIL_MST \
(  \
    PARTNER_SCHOOLCD    varchar(12)  not null, \
    PARTNER_SCHOOL_SEQ  varchar(3)   not null, \
    REMARK1             varchar(90), \
    REMARK2             varchar(90), \
    REMARK3             varchar(90), \
    REMARK4             varchar(90), \
    REMARK5             varchar(90), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PARTNER_SCHOOL_DETAIL_MST add constraint PK_PRTNSCHOOL_DT_M \
primary key (PARTNER_SCHOOLCD, PARTNER_SCHOOL_SEQ)
