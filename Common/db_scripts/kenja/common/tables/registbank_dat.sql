-- kanji=����
-- $Id: 7f9a9b60f215659a7e0c17a6acc1e3a5a99f9656 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��Ͽ��ԥǡ���
drop table REGISTBANK_DAT

create table REGISTBANK_DAT \
( \
        "SCHOOLCD"       varchar(12) not null, \
        "SCHREGNO"       varchar(8)  not null, \
        "SEQ"            varchar(1)  not null, \
        "BANKCD"         varchar(4),  \
        "BRANCHCD"       varchar(3),  \
        "DEPOSIT_ITEM"   varchar(1),  \
        "ACCOUNTNO"      varchar(7),  \
        "ACCOUNTNAME"    varchar(120), \
        "RELATIONSHIP"   varchar(2),  \
        "PAID_INFO_CD"   varchar(2),  \
        "REGISTERCD"     varchar(10),  \
        "UPDATED"        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REGISTBANK_DAT add constraint PK_REGISTBANK_DAT primary key (SCHOOLCD, SCHREGNO, SEQ)
