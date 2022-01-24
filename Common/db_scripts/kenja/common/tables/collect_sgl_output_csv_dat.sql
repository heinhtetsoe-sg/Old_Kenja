-- kanji=����
-- $Id: 2a6634fe2faf82d8f23e4341dec7aef3c39e3901 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

--SIGEL�������¶���¿��ؾ���ǡ���

drop table COLLECT_SGL_OUTPUT_CSV_DAT

create table COLLECT_SGL_OUTPUT_CSV_DAT \
( \
        "YEAR"                  varchar(4)  not null, \
        "DIRECT_DEBIT"          varchar(4)  not null, \
        "SEQ"                   int         not null, \
        "ACCOUNTNAME"           varchar(120), \
        "ACCOUNTNAME_KANJI"     varchar(120), \
        "SCHREGNO"              varchar(20)  not null, \
        "PLAN_MONEY"            varchar(10) , \
        "TOTAL_MONEY"           varchar(10) , \
        "BANKCD"                varchar(4)  , \
        "BANKNAME"              varchar(45) , \
        "BANKNAME_KANJI"        varchar(120), \
        "BRANCHCD"              varchar(3)  , \
        "BRANCHNAME"            varchar(45) , \
        "BRANCHNAME_KANJI"      varchar(120), \
        "DEPOSIT_DIV"           varchar(30) , \
        "ACCOUNTNO"             varchar(7)  , \
        "RESULT_CD"             varchar(30) , \
        "SUMMARY"               varchar(30) , \
        "TOROKUNO"              varchar(30) , \
        "REGISTERCD"            varchar(10) , \
        "UPDATED"               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SGL_OUTPUT_CSV_DAT \
add constraint PK_C_SGL_OUT_CSVD \
primary key \
(YEAR, DIRECT_DEBIT, SEQ)
