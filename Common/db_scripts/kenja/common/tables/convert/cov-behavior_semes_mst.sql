-- kanji=����
-- $Id: 89a0cad29ee5c82e602a2512b2a206297db71ad0 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

insert into BEHAVIOR_SEMES_MST \
(YEAR, GRADE, CODE, CODENAME, VIEWNAME, REGISTERCD, UPDATED) \
select \
    t2.YEAR, \
    t2.GRADE, \
    t1.NAMECD2 as CODE, \
    t1.NAME1 as CODENAME, \
    cast(null as VARCHAR(150)) as VIEWNAME, \
    'alp' as REGISTERCD, \
    current timestamp as UPDATED \
from \
    NAME_MST t1, \
    SCHREG_REGD_GDAT t2 \
where \
    t1.NAMECD1 = 'D035'

