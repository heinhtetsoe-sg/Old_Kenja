-- kanji=����
-- $Id: 3cbc355ab108e2a4c5e230c28c7bd441e01ffae1 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--��ɼ�ǡ���
DROP TABLE COLLECT_REPAY_SLIP_DAT

CREATE TABLE COLLECT_REPAY_SLIP_DAT \
( \
    SCHOOLCD              varchar(12) not null, \
    SCHOOL_KIND           varchar(2)  not null, \
    YEAR                  varchar(4)  not null, \
    REPAY_SLIP_NO         varchar(15) not null, \
    SCHREGNO              varchar(8)  not null, \
    REPAY_DATE            date,  \
    REPAY_DIV             varchar(1),  \
    REPAY_MONEY           integer, \
    REPAID_DATE           date, \
    APPROVAL              varchar(1),  \
    CANCEL_FLG            varchar(1),  \
    CANCEL_DATE           date, \
    REGISTERCD            varchar(10), \
    UPDATED               timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table COLLECT_REPAY_SLIP_DAT \
add constraint PK_COL_REPAY_SLIP \
primary key \
(SCHOOLCD, SCHOOL_KIND, YEAR, REPAY_SLIP_NO)
