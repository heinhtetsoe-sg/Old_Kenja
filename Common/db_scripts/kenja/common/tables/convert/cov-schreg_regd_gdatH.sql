-- kanji=漢字
-- $Id: 4b3db080c01174047a56e172f33f6775d8e6d61c $
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
    'H', \
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
