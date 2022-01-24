-- kanji=漢字
-- $Id: 6eba6ff2b3b26a86b2c647cf4a372370f43b742d $
-- 学籍在籍データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
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
