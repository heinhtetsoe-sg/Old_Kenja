-- kanji=����
-- $Id: d22cc169eea9d904fa72bb33cfa03e24cf0248a4 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--���䶨�ե����ޥåȡʥإå������쥳���ɡ�

drop table COLLECT_ZENGIN_HEADER_RECORD_DAT

create table COLLECT_ZENGIN_HEADER_RECORD_DAT \
( \
        "YEAR"                  varchar(4) not null, \
        "DATA_DIV"              varchar(1) , \
        "TYPE_CD"               varchar(2) , \
        "CD_DIV"                varchar(1) , \
        "CLIENT_CD"             varchar(10), \
        "CLIENT_NAME"           varchar(120), \
        "DIRECT_DEBIT"          varchar(4) not null, \
        "T_BANKCD"              varchar(4) , \
        "T_BANKNAME"            varchar(45), \
        "T_BRANCHCD"            varchar(3) , \
        "T_BRANCHNAME"          varchar(45), \
        "DEPOSIT_DIV"           varchar(1) , \
        "T_ACCOUNTNO"           varchar(7) , \
        "DUMMY"                 varchar(17), \
        "OUTPUT_FLG"            varchar(1) , \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_ZENGIN_HEADER_RECORD_DAT \
add constraint PK_C_ZEN_HED_RC_D \
primary key \
(YEAR, DIRECT_DEBIT)
