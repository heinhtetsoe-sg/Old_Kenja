-- kanji=����
-- $Id: 963dc1c5fb88a2b16a4b055d1cc4a1fa899173d6 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback
--
DELETE FROM APPOINTED_DAY_MST

INSERT INTO APPOINTED_DAY_MST \
SELECT \
    YEAR, \
    MONTH, \
    SEMESTER, \
    max(APPOINTED_DAY) as APPOINTED_DAY, \
    max(REGISTERCD) as REGISTERCD, \
    max(UPDATED) as UPDATED \
FROM ATTEND_SEMES_DAT \
WHERE APPOINTED_DAY is not null \
GROUP BY YEAR, MONTH, SEMESTER
