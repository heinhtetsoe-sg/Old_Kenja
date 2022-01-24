-- kanji=漢字
-- $Id: 39146a9f887412961ca8ec50cc0146a237fc4031 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table MOCK_CENTER_SUBCLASS_MST

create table MOCK_CENTER_SUBCLASS_MST ( \
    YEAR               varchar(4)  not null, \
    CENTER_CLASS_CD    varchar(2)  not null, \
    CENTER_SUBCLASS_CD varchar(6)  not null, \
    BUNRIDIV           varchar(1)  not null, \
    SUBCLASS_NAME      varchar(60), \
    SUBCLASS_ABBV      varchar(15), \
    CLASSCD            varchar(2), \
    SCHOOL_KIND        varchar(2), \
    CURRICULUM_CD      varchar(2), \
    SUBCLASSCD         varchar(6), \
    SUBCLASS_DIV       varchar(1), \
    PERFECT            smallint, \
    ALLOT_POINT        smallint, \
    REGISTERCD         varchar(10), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CENTER_SUBCLASS_MST add constraint PK_MOCK_CENTER_S_M \
        primary key (YEAR, CENTER_CLASS_CD, CENTER_SUBCLASS_CD)
