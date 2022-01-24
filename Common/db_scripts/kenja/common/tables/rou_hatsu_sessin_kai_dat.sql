-- kanji=����
-- $Id: 79950d8d4fcf5bdb018f001e78df6190ca615a58 $

-- �����ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table ROU_HATSU_SESSIN_KAI_DAT

create table ROU_HATSU_SESSIN_KAI_DAT ( \
    YEAR                varchar(4)  not null, \
    SCHREGNO            varchar(8)  not null, \
    KAIKIN_FLG          varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ROU_HATSU_SESSIN_KAI_DAT add constraint PK_ROU_HATSU_SESSIN_KAI_DAT primary key (YEAR, SCHREGNO)
