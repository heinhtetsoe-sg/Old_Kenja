-- kanji=����
-- $Id: d5ce021ee52bec896ee682924b9d0faf1dbce4f8 $
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
    'J', \
    RTRIM(CHAR(INT(GRADE))), \
    RTRIM(CHAR(INT(GRADE))), \
    cast(NULL AS VARCHAR(60)), \
    cast(NULL AS VARCHAR(60)), \
    'Alp', \
    sysdate() \
from \
    SCHREG_REGD_HDAT \
group by \
    YEAR, \
    GRADE)
