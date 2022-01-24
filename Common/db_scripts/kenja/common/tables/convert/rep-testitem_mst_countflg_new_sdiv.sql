-- $Id: 0569ef6c178b028def271b57a704335ab8b3723a $

DROP TABLE TESTITEM_MST_COUNTFLG_NEW_SDIV_OLD
RENAME TABLE TESTITEM_MST_COUNTFLG_NEW_SDIV TO TESTITEM_MST_COUNTFLG_NEW_SDIV_OLD
CREATE TABLE TESTITEM_MST_COUNTFLG_NEW_SDIV ( \
    YEAR            VARCHAR(4) NOT NULL, \
    SEMESTER        VARCHAR(1) NOT NULL, \
    TESTKINDCD      VARCHAR(2) NOT NULL, \
    TESTITEMCD      VARCHAR(2) NOT NULL, \
    SCORE_DIV       VARCHAR(2) NOT NULL, \
    TESTITEMNAME    VARCHAR(30), \
    TESTITEMABBV1   VARCHAR(30), \
    TESTITEMABBV2   VARCHAR(30), \
    TESTITEMABBV3   VARCHAR(30), \
    COUNTFLG        VARCHAR(1), \
    SEMESTER_DETAIL VARCHAR(1), \
    SIDOU_INPUT     VARCHAR(1), \
    SIDOU_INPUT_INF VARCHAR(1), \
    MIKOMI_FLG      VARCHAR(1), \
    SANKOU_FLG      VARCHAR(1), \
    REMARK_FLG      VARCHAR(1), \
    JYORETSU_FLG    VARCHAR(1), \
    NOT_USE_CSV_FLG VARCHAR(1), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO TESTITEM_MST_COUNTFLG_NEW_SDIV \
    SELECT \
        YEAR  , \
        SEMESTER  , \
        TESTKINDCD, \
        TESTITEMCD, \
        SCORE_DIV , \
        TESTITEMNAME   , \
        TESTITEMABBV1  , \
        TESTITEMABBV2  , \
        TESTITEMABBV3  , \
        COUNTFLG , \
        SEMESTER_DETAIL , \
        SIDOU_INPUT , \
        SIDOU_INPUT_INF , \
        CAST(NULL AS VARCHAR(1)) AS MIKOMI_FLG, \
        CAST(NULL AS VARCHAR(1)) AS SANKOU_FLG, \
        CAST(NULL AS VARCHAR(1)) AS REMARK_FLG, \
        JYORETSU_FLG, \
        NOT_USE_CSV_FLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        TESTITEM_MST_COUNTFLG_NEW_SDIV_OLD

ALTER TABLE TESTITEM_MST_COUNTFLG_NEW_SDIV ADD CONSTRAINT PK_TITEM_M_CF_N_SD \
      PRIMARY KEY (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV)