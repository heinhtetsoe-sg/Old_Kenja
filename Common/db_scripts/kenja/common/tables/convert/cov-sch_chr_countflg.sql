-- kanji=����
-- $Id: 7d898035284a469f72457582cd26324b1d727047 $

-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

INSERT INTO SCH_CHR_COUNTFLG  \
SELECT \
    T1.EXECUTEDATE, \
    T1.PERIODCD, \
    T1.CHAIRCD, \
    T3.TRGTGRADE AS GRADE, \
    T3.TRGTCLASS AS HR_CLASS, \
    T2.COUNTFLG, \
    '00999999' AS REGISTERCD, \
    sysdate() AS UPDATED \
FROM \
    SCH_CHR_DAT T1, \
    CHAIR_DAT T2, \
    CHAIR_CLS_DAT T3 \
WHERE \
    T3.YEAR=T2.YEAR AND \
    T3.SEMESTER=T2.SEMESTER AND \
    (T3.CHAIRCD=T2.CHAIRCD OR T3.CHAIRCD='0000000') AND \
    T3.GROUPCD=T2.GROUPCD AND \
    T2.YEAR=T1.YEAR AND \
    T2.SEMESTER=T1.SEMESTER AND \
    T2.CHAIRCD=T1.CHAIRCD AND \
    T1.YEAR='2009' AND \
    NOT EXISTS (SELECT 'X' FROM SCH_CHR_COUNTFLG W1 \
                 WHERE W1.EXECUTEDATE=T1.EXECUTEDATE \
                   AND W1.PERIODCD=T1.PERIODCD \
                   AND W1.CHAIRCD=T1.CHAIRCD)
