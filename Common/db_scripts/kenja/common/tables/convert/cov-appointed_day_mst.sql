-- kanji=漢字
-- $Id: 963dc1c5fb88a2b16a4b055d1cc4a1fa899173d6 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
