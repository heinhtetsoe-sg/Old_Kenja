-- kanji=����
-- $Id: 890d9594344862d6f324a7b00f829dd7c87d7da0 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--

insert into FACILITY_GATE_DAT \
    (FACCD, GATENO, UPDATED) \
select \
    FACCD, FACCD AS GATENO, current timestamp \
from \
    FACILITY_MST


