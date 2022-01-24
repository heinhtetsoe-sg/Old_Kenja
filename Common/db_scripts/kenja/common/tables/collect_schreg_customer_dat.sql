-- kanji=����
-- $Id: 8669a27e986f8fff8800eabfba348610a432f156 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table COLLECT_SCHREG_CUSTOMER_DAT

create table COLLECT_SCHREG_CUSTOMER_DAT \
( \
    SCHREGNO            varchar(8)  not null, \
    CUSTOMER_NUMBER     varchar(20), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_SCHREG_CUSTOMER_DAT \
add constraint PK_COLL_SCH_CUS_D \
primary key \
(SCHREGNO)
