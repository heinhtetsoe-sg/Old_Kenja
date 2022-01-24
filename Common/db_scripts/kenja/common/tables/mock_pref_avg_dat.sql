-- kanji=漢字
-- $Id: 7f34e4787a80a3cc40ad66c83153837c15bf57de $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MOCK_PREF_AVG_DAT

create table MOCK_PREF_AVG_DAT ( \
    YEAR                varchar(4) not null, \
    MOCKCD              varchar(9) not null, \
    GRADE               varchar(2) not null, \
    PREF_SUBCLASSCD     varchar(6) not null, \
    AVG                 decimal (9,5), \
    SCORE               integer, \
    CNT                 integer, \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_PREF_AVG_DAT add constraint PK_MOCK_PREF_AVG_D \
        primary key (YEAR, MOCKCD, GRADE, PREF_SUBCLASSCD)
