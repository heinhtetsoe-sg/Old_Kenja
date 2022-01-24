-- kanji=����
-- $Id: 6eba6ff2b3b26a86b2c647cf4a372370f43b742d $
-- ���Һ��ҥǡ���

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--


insert into SCHREG_REGD_GDAT \
(select \
    YEAR, \
    GRADE, \
    CASE WHEN GRADE < '04' \
         THEN 'J' \
         ELSE 'H' \
    END, \
    CASE WHEN GRADE < '04' \
         THEN RTRIM(CHAR(INT(GRADE))) \
         ELSE RTRIM(CHAR(INT(GRADE) - 3)) \
    END, \
    CHAR(INT(GRADE)), \
    cast(NULL AS VARCHAR(60)), \
    cast(NULL AS VARCHAR(60)), \
    'Alp', \
    sysdate() \
from \
    SCHREG_REGD_HDAT \
group by \
    YEAR, \
    GRADE)
