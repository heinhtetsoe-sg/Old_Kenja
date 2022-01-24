-- kanji=漢字
-- $Id: 16a6091645ed60121ed92afb5b8a97f44e08ec47 $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table MOCK_DOCUMENT_DAT

create table MOCK_DOCUMENT_DAT ( \
    YEAR                varchar(4) not null, \
    MOCKCD              varchar(9) not null, \
    GRADE               varchar(2) not null, \
    COURSECD            varchar(1) not null, \
    MAJORCD             varchar(3) not null, \
    COURSECODE          varchar(4) not null, \
    MOCK_SUBCLASS_CD    varchar(6) not null, \
    FOOTNOTE            varchar(1050), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_DOCUMENT_DAT add constraint PK_MOCK_DOC_DAT \
      primary key (YEAR, MOCKCD, GRADE, COURSECD, MAJORCD, COURSECODE, MOCK_SUBCLASS_CD)
