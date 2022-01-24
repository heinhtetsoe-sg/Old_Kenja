-- kanji=漢字
-- $Id: 6911c3f85599196f91cb8ec0c074900a5023f47c $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table MOCK_RANK_DAT

create table MOCK_RANK_DAT ( \
    YEAR             varchar(4) not null, \
    MOCKCD           varchar(9) not null, \
    SCHREGNO         varchar(8) not null, \
    MOCK_SUBCLASS_CD varchar(6) not null, \
    MOCKDIV          varchar(1) not null, \
    SCORE            smallint, \
    AVG              decimal (8,5), \
    GRADE_RANK       smallint, \
    GRADE_DEVIATION  decimal (4,1), \
    CLASS_RANK       smallint, \
    CLASS_DEVIATION  decimal (4,1), \
    COURSE_RANK      smallint, \
    COURSE_DEVIATION decimal (4,1), \
    REGISTERCD       varchar(8), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_RANK_DAT add constraint PK_MOCK_RANK_DAT \
      primary key (YEAR, MOCKCD, SCHREGNO, MOCK_SUBCLASS_CD, MOCKDIV)
