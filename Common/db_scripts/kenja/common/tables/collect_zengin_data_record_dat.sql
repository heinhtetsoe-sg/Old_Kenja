-- kanji=����
-- $Id: 98bfbf3b38dfdabf934bc79b048945307247d0b0 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--���䶨�ե����ޥåȡʥǡ������쥳���ɡ�

drop table COLLECT_ZENGIN_DATA_RECORD_DAT

create table COLLECT_ZENGIN_DATA_RECORD_DAT \
( \
        "YEAR"                  varchar(4) not null, \
        "DIRECT_DEBIT"          varchar(4) not null, \
        "SEQ"                   int        not null, \
        "DATA_DIV"              varchar(1) , \
        "BANKCD"                varchar(4) , \
        "BANKNAME"              varchar(45), \
        "BRANCHCD"              varchar(3) , \
        "BRANCHNAME"            varchar(45), \
        "DUMMY1"                varchar(4) , \
        "DEPOSIT_DIV"           varchar(1) , \
        "ACCOUNTNO"             varchar(7) , \
        "ACCOUNTNAME"           varchar(120), \
        "PLAN_MONEY"            varchar(10), \
        "NEW_CD"                varchar(1) , \
        "CUSTOMER_CD"           varchar(20), \
        "RESULT_CD"             varchar(1) , \
        "DUMMY2"                varchar(8) , \
        "REGISTERCD"            varchar(10), \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_ZENGIN_DATA_RECORD_DAT \
add constraint PK_C_ZEN_DAT_RC_D \
primary key \
(YEAR, DIRECT_DEBIT, SEQ)
