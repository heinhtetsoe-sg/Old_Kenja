-- kanji=����
-- $Id: 0f20681712707fb1caefb26cfc8ab51b75984ab5 $
-- �з礱�ǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
drop   table ATTEND_DAY_SUBL_DAT

create table ATTEND_DAY_SUBL_DAT ( \
    SCHREGNO        varchar(8) not null, \
    ATTENDDATE      date not null, \
    DI_CD           varchar(2)  not null, \
    SUBL_CD         varchar(3) not null, \
    YEAR            varchar(4) not null, \
    REGISTERCD      varchar(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_DAY_SUBL_DAT add constraint PK_AT_DAY_SUBL_DAT \
        primary key (SCHREGNO, ATTENDDATE)
