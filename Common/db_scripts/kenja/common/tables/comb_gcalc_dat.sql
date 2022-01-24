-- kanji=漢字
-- $Id: 8b45f85eb56e184f6b942e24c4b8dcb9130271bc $
-- 合併科目別評定計算方法設定データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table COMB_GCALC_DAT

create table COMB_GCALC_DAT(  \
    YEAR                        VARCHAR(4) NOT NULL, \
    COMBINED_CLASSCD            VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    GVAL_CALC                   VARCHAR(1), \
    REGISTERCD                  VARCHAR(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table COMB_GCALC_DAT add constraint PK_COM_GC_DAT \
primary key (YEAR, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD)
