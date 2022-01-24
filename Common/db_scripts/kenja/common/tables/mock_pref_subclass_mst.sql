-- kanji=漢字
-- $Id: 24cbdab4767b26b51bd9fe4263e8b75e321d2127 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MOCK_PREF_SUBCLASS_MST

create table MOCK_PREF_SUBCLASS_MST ( \
    PREF_SUBCLASSCD    VARCHAR(6) NOT NULL, \
    SUBCLASS_NAME      VARCHAR(60), \
    SUBCLASS_ABBV      VARCHAR(60), \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table MOCK_PREF_SUBCLASS_MST add constraint PK_MOCK_PREF_SUB_M \
        primary key (PREF_SUBCLASSCD)
