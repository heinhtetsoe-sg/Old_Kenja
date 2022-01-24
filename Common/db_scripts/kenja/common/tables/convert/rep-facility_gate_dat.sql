-- kanji=漢字
-- $Id: 890d9594344862d6f324a7b00f829dd7c87d7da0 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

insert into FACILITY_GATE_DAT \
    (FACCD, GATENO, UPDATED) \
select \
    FACCD, FACCD AS GATENO, current timestamp \
from \
    FACILITY_MST


