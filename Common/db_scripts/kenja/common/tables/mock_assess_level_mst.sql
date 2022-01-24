-- kanji=漢字
-- $Id: f9e3e9b6f37bf00499e4c57627c65d84b233ff3b $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table MOCK_ASSESS_LEVEL_MST

create table MOCK_ASSESS_LEVEL_MST ( \
    YEAR                varchar(4) not null, \
    MOCKCD              varchar(9) not null, \
    MOCK_SUBCLASS_CD    varchar(6) not null, \
    DIV                 varchar(1) not null, \
    GRADE               varchar(2) not null, \
    HR_CLASS            varchar(3) not null, \
    COURSECD            varchar(1) not null, \
    MAJORCD             varchar(3) not null, \
    COURSECODE          varchar(4) not null, \
    ASSESSLEVEL         smallint not null, \
    ASSESSMARK          varchar(6), \
    ASSESSLOW           decimal, \
    ASSESSHIGH          decimal, \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_ASSESS_LEVEL_MST add constraint pk_mock_ass_lvl_m \
      primary key (YEAR, MOCKCD, MOCK_SUBCLASS_CD, DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE, ASSESSLEVEL)
