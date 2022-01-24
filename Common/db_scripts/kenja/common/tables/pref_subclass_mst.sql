-- kanji=漢字
-- $Id: 9b038ed9d61297deeb920cf790fa207f95d7a73b $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table PREF_SUBCLASS_MST

create table PREF_SUBCLASS_MST ( \
    PREF_SUBCLASSCD    VARCHAR(6) NOT NULL, \
    SUBCLASS_NAME      VARCHAR(60), \
    SUBCLASS_ABBV      VARCHAR(60), \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table PREF_SUBCLASS_MST add constraint PK_PREF_SUBCLASS_M \
        primary key (PREF_SUBCLASSCD)
