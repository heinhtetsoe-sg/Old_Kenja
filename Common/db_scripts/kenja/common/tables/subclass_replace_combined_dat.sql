-- kanji=漢字
-- $Id: c8c0055b42df3e00b786fd2feae3744d95022797 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table SUBCLASS_REPLACE_COMBINED_DAT

create table SUBCLASS_REPLACE_COMBINED_DAT ( \
    REPLACECD                 VARCHAR(1) NOT NULL, \
    YEAR                      VARCHAR(4) NOT NULL, \
    COMBINED_CLASSCD          VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND      VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD    VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD       VARCHAR(6) NOT NULL, \
    ATTEND_CLASSCD            VARCHAR(2) NOT NULL, \
    ATTEND_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    ATTEND_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    ATTEND_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    CALCULATE_CREDIT_FLG      VARCHAR(1), \
    STUDYREC_CREATE_FLG       VARCHAR(1), \
    PRINT_FLG1                VARCHAR(1), \
    PRINT_FLG2                VARCHAR(1), \
    PRINT_FLG3                VARCHAR(1), \
    WEIGHTING                 DECIMAL(3,2), \
    REGISTERCD                VARCHAR(8), \
    UPDATED                   TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table SUBCLASS_REPLACE_COMBINED_DAT add constraint PK_SUBREPCOMB_DAT \
        primary key (YEAR, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD)
