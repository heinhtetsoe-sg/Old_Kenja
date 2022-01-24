-- kanji=����
-- $Id: b2d39f4ae2e7997d1ec48af88a86752be34e1459 $
-- ���з�ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ATTEND2_DAT

create table ATTEND2_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    PERIODCD        varchar(1) not null, \
    CHAIRCD         varchar(7), \
    DI_CD           varchar(2), \
    YEAR            varchar(4), \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND2_DAT add constraint PK_ATTEND2_DAT \
        primary key (SCHREGNO, ATTENDDATE, PERIODCD)
