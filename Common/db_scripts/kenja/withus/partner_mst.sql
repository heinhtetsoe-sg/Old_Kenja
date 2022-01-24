-- kanji=����
-- $Id: partner_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

drop table PARTNER_MST

create table PARTNER_MST \
(  \
        PARTNER_CD      varchar(4) not null, \
        PARTNER_NAME    varchar(90), \
        PARTNER_ZIPCD   varchar(8), \
        PREF_CD         varchar(2), \
        PARTNER_ADDR1   varchar(75), \
        PARTNER_ADDR2   varchar(75), \
        PARTNER_ADDR3   varchar(75), \
        PARTNER_TELNO   varchar(14), \
        REGISTERCD      varchar(8), \
        UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PARTNER_MST  \
add constraint PK_PARTNER_MST  \
primary key  \
(PARTNER_CD)
