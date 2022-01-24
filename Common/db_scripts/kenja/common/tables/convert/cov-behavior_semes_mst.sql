-- kanji=漢字
-- $Id: 89a0cad29ee5c82e602a2512b2a206297db71ad0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback

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

