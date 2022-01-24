-- kanji=漢字
-- $Id: 88ed77229db617016d62a400841fe195d0d14390 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table MOCK_SUBCLASS_MST_OLD
create table MOCK_SUBCLASS_MST_OLD like MOCK_SUBCLASS_MST
insert into  MOCK_SUBCLASS_MST_OLD select * from MOCK_SUBCLASS_MST

drop   table MOCK_SUBCLASS_MST
create table MOCK_SUBCLASS_MST ( \
    MOCK_SUBCLASS_CD   VARCHAR(6) NOT NULL, \
    SUBCLASS_NAME      VARCHAR(60), \
    SUBCLASS_ABBV      VARCHAR(15), \
    CLASSCD            VARCHAR(2), \
    SCHOOL_KIND        VARCHAR(2), \
    CURRICULUM_CD      VARCHAR(2), \
    SUBCLASSCD         VARCHAR(6), \
    PREF_SUBCLASSCD    VARCHAR(6), \
    SUBCLASS_DIV       VARCHAR(1), \
    REGISTERCD         VARCHAR(10), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table MOCK_SUBCLASS_MST add constraint PK_MOCK_SUBCLASS_M \
        primary key (MOCK_SUBCLASS_CD)

insert into MOCK_SUBCLASS_MST \
    SELECT \
        MOCK_SUBCLASS_CD, \
        SUBCLASS_NAME, \
        SUBCLASS_ABBV, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        PREF_SUBCLASSCD, \
        CAST(NULL AS VARCHAR(1)) AS SUBCLASS_DIV, \
        REGISTERCD, \
        UPDATED \
    FROM \
        MOCK_SUBCLASS_MST_OLD
