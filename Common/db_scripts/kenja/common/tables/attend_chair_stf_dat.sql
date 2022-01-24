-- kanji=����
-- $Id: 85d83d2a9823cef9b6a187b5bda39ac29a72d08d $
-- �з礱�ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ATTEND_CHAIR_STF_DAT

create table ATTEND_CHAIR_STF_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    PERIODCD        varchar(1) not null, \
    CHAIRCD         varchar(7), \
    DI_CD           varchar(2), \
    DI_REMARK_CD    varchar(3), \
    DI_REMARK       varchar(60), \
    YEAR            varchar(4), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_CHAIR_STF_DAT add constraint PK_ATTEND_CHAIR_STF_DAT \
        primary key (SCHREGNO, ATTENDDATE, PERIODCD)
