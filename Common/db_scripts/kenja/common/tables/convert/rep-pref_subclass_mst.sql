-- kanji=漢字
-- $Id: 737c0fdd445987c1be402ce4e11c716502294c73 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table PREF_SUBCLASS_MST_OLD
create table PREF_SUBCLASS_MST_OLD like PREF_SUBCLASS_MST
insert into  PREF_SUBCLASS_MST_OLD select * from PREF_SUBCLASS_MST

drop   table PREF_SUBCLASS_MST
create table PREF_SUBCLASS_MST ( \
    PREF_SUBCLASSCD   VARCHAR(6) NOT NULL, \
    SUBCLASS_NAME      VARCHAR(60), \
    SUBCLASS_ABBV      VARCHAR(60), \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP \
) in usr1dms index in idx1dms

alter table PREF_SUBCLASS_MST add constraint PK_PREF_SUBCLASS_M \
        primary key (PREF_SUBCLASSCD)

insert into PREF_SUBCLASS_MST \
    SELECT \
        PREF_SUBCLASSCD, \
        SUBCLASS_NAME, \
        SUBCLASS_ABBV, \
        registercd, \
        updated \
    FROM \
        PREF_SUBCLASS_MST_OLD
