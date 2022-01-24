-- kanji=����
-- $Id: ae4c428b5453bac9b65ca818491949af912e9d77 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--���䶨�ե����ޥåȡʥ���ɡ��쥳���ɡ�

drop table COLLECT_ZENGIN_END_RECORD_DAT

create table COLLECT_ZENGIN_END_RECORD_DAT \
( \
        "YEAR"              varchar(4) not null, \
        "DIRECT_DEBIT"      varchar(4) not null, \
        "DATA_DIV"          varchar(1)  , \
        "DUMMY"             varchar(119), \
        "REGISTERCD"        varchar(10) , \
        "UPDATED"           timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_ZENGIN_END_RECORD_DAT \
add constraint PK_C_ZEN_END_RC_D \
primary key \
(YEAR, DIRECT_DEBIT)
