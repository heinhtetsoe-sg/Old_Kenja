-- kanji=漢字
-- $Id: 10583e67c05b7ab7c63d0653d67b1dd5cdd9746b $
-- 合併先科目自動算出実行履歴データ（パーツ）（重み）

drop table SUBCLASS_WEIGHTING_RIREKI_DAT

create table SUBCLASS_WEIGHTING_RIREKI_DAT(  \
    CALC_DATE                   DATE NOT NULL, \
    CALC_TIME                   TIME NOT NULL, \
    COMBINED_CLASSCD            VARCHAR(2) NOT NULL, \
    COMBINED_SCHOOL_KIND        VARCHAR(2) NOT NULL, \
    COMBINED_CURRICULUM_CD      VARCHAR(2) NOT NULL, \
    COMBINED_SUBCLASSCD         VARCHAR(6) NOT NULL, \
    YEAR                        VARCHAR(4), \
    SEMESTER                    VARCHAR(1), \
    TESTKINDCD                  VARCHAR(2), \
    TESTITEMCD                  VARCHAR(2), \
    SCORE_DIV                   VARCHAR(2), \
    FLG                         VARCHAR(1), \
    GRADE                       VARCHAR(2), \
    COURSECD                    VARCHAR(1), \
    MAJORCD                     VARCHAR(3), \
    COURSECODE                  VARCHAR(4), \
    REGISTERCD                  VARCHAR(10), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table SUBCLASS_WEIGHTING_RIREKI_DAT add constraint PK_SUB_WEI_RIR_D \
primary key (CALC_DATE, CALC_TIME, COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD)
