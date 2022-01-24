-- kanji=����
-- $Id: 177fc5b5ba8011fd69d1eae0eb89e0db576e2f2f $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table GUARDIAN_COMMITTEE_DAT

create table GUARDIAN_COMMITTEE_DAT ( \
    YEAR                varchar(4) not null, \
    SCHREGNO            varchar(8) not null, \
    DIV                 varchar(1) not null, \
    EXECUTIVECD         varchar(2) not null, \
    GUARD_NAME_DIV      varchar(1), \
    REMARK              varchar(75), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table GUARDIAN_COMMITTEE_DAT add constraint PK_G_COM_DAT primary key (YEAR, SCHREGNO, DIV, EXECUTIVECD)
