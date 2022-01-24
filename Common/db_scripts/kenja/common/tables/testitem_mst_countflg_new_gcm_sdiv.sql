-- kanji=漢字
-- $Id: 0b7fae014aa3b6ad8c083ae35220fb925544098a $

DROP   TABLE TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV

CREATE TABLE TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ( \
    YEAR             VARCHAR(4)   NOT NULL, \
    SCHOOLCD         VARCHAR(12)  NOT NULL, \
    SCHOOL_KIND      VARCHAR(2)   NOT NULL, \
    SEMESTER         VARCHAR(1)   NOT NULL, \
    TESTKINDCD       VARCHAR(2)   NOT NULL, \
    TESTITEMCD       VARCHAR(2)   NOT NULL, \
    SCORE_DIV        VARCHAR(2)   NOT NULL, \
    GRADE            VARCHAR(2)   NOT NULL, \
    COURSECD         VARCHAR(1)   NOT NULL, \
    MAJORCD          VARCHAR(3)   NOT NULL, \
    TESTITEMNAME     VARCHAR(30) , \
    TESTITEMABBV1    VARCHAR(30) , \
    TESTITEMABBV2    VARCHAR(30) , \
    TESTITEMABBV3    VARCHAR(30) , \
    COUNTFLG         VARCHAR(1)  , \
    SEMESTER_DETAIL  VARCHAR(1)  , \
    SIDOU_INPUT      VARCHAR(1)  , \
    SIDOU_INPUT_INF  VARCHAR(1)  , \
    JYORETSU_FLG     VARCHAR(1)  , \
    NOT_USE_CSV_FLG  VARCHAR(1)  , \
    TEST_START_DATE  DATE        , \
    TEST_END_DATE    DATE        , \
    REGISTERCD       VARCHAR(10) , \
    UPDATED          TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS
 
ALTER TABLE TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ADD CONSTRAINT PK_TIMCFNW_GCM_SDV PRIMARY KEY (YEAR,SCHOOLCD,SCHOOL_KIND,SEMESTER,TESTKINDCD,TESTITEMCD,SCORE_DIV,GRADE,COURSECD,MAJORCD)