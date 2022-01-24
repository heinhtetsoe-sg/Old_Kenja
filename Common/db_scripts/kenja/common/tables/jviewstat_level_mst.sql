-- kanji=漢字
-- $Id: 1c6fd36c03afd32cc61ed75c76f6b266866a5875 $
-- テスト項目マスタ集計フラグ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table JVIEWSTAT_LEVEL_MST

create table JVIEWSTAT_LEVEL_MST ( \
    YEAR            VARCHAR(4)  NOT NULL, \
    CLASSCD         VARCHAR(2)  NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)  NOT NULL, \
    CURRICULUM_CD   VARCHAR(2)  NOT NULL, \
    SUBCLASSCD      VARCHAR(6)  NOT NULL, \
    VIEWCD          VARCHAR(4)  NOT NULL, \
    DIV             VARCHAR(1)  NOT NULL, \
    GRADE           VARCHAR(2)  NOT NULL, \
    ASSESSLEVEL     SMALLINT    NOT NULL, \
    ASSESSMARK      VARCHAR(6), \
    ASSESSLOW       DECIMAL, \
    ASSESSHIGH      DECIMAL, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table JVIEWSTAT_LEVEL_MST add constraint pk_jvs_lvl_mst \
      primary key (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD, DIV, GRADE, ASSESSLEVEL)