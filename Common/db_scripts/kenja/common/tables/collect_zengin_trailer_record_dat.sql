-- kanji=����
-- $Id: e446fadf2c9dd1386f9f530c0f9d67e963ea6ff3 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--���䶨�ե����ޥåȡʥȥ졼�顦�쥳���ɡ�

drop table COLLECT_ZENGIN_TRAILER_RECORD_DAT

create table COLLECT_ZENGIN_TRAILER_RECORD_DAT \
( \
        "YEAR"                  varchar(4) not null, \
        "DIRECT_DEBIT"          varchar(4) not null, \
        "DATA_DIV"              varchar(1) , \
        "TOTAL_CNT"             varchar(6) , \
        "TOTAL_MONEY"           varchar(12), \
        "TRANSFER_CNT"          varchar(6) , \
        "TRANSFER_MONEY"        varchar(12), \
        "NOT_TRANSFER_CNT"      varchar(6) , \
        "NOT_TRANSFER_MONEY"    varchar(12), \
        "DUMMY"                 varchar(65), \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_ZENGIN_TRAILER_RECORD_DAT \
add constraint PK_C_ZEN_TRA_RC_D \
primary key \
(YEAR, DIRECT_DEBIT)
