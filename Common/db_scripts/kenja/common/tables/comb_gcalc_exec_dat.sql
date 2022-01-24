-- kanji=漢字
-- $Id: 60c4d1bfdf857181ab6bb983c0cee648144502b6 $
-- 科目合併評定自動計算履歴データ

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table COMB_GCALC_EXEC_DAT

create table COMB_GCALC_EXEC_DAT(  \
    CALC_DATE                   DATE NOT NULL, \
    CALC_TIME                   TIME NOT NULL, \
    COMBINED_CLASSCD            VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    GVAL_CALC                   VARCHAR(1), \
    YEAR                        VARCHAR(4), \
    REGISTERCD                  VARCHAR(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table COMB_GCALC_EXEC_DAT add constraint PK_COM_GC_EXE_DAT \
primary key (CALC_DATE, CALC_TIME, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD)
