-- kanji=����
-- $Id: 67f6c691ce6f75cdc7c2c01ec2f20faba1c51b23 $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table GUARDIAN_COMMITTEE_YDAT

create table GUARDIAN_COMMITTEE_YDAT ( \
    YEAR                varchar(4) not null, \
    DIV                 varchar(1) not null, \
    EXECUTIVECD         varchar(2) not null, \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GUARDIAN_COMMITTEE_YDAT add constraint PK_G_COM_YDAT primary key (YEAR, DIV, EXECUTIVECD)
