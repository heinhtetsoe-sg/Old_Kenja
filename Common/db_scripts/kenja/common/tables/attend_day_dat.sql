-- kanji=����
-- $Id: 64b4e7443c723cb782b97ed66ac17c3f3cd84912 $
-- �з礱�ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ATTEND_DAY_DAT

create table ATTEND_DAY_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    DI_CD           varchar(2) not null, \
    DI_REMARK       varchar(60), \
    YEAR            varchar(4) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_DAY_DAT add constraint PK_AT_DAY_DAT \
        primary key (SCHREGNO, ATTENDDATE, DI_CD)
