-- kanji=����
-- $Id: 0151c5621a55d7f745bd11eba53ca491cf4bce80 $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table GUARDIAN_COMMITTEE_MST

create table GUARDIAN_COMMITTEE_MST ( \
    DIV                 varchar(1) not null, \
    EXECUTIVECD         varchar(2) not null, \
    NAME                varchar(75), \
    ABBV                varchar(75), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GUARDIAN_COMMITTEE_MST add constraint PK_G_COM_MST primary key (DIV, EXECUTIVECD)
