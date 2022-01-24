-- kanji=漢字
-- $Id: d9ee9af6a83565aa09f9f83d737d0d3f4827b78e $

DROP   TABLE TESTITEM_MST_COUNTFLG_NEW_SDIV
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

ALTER TABLE TESTITEM_MST_COUNTFLG_NEW_SDIV ADD CONSTRAINT PK_TITEM_M_CF_N_SD \
      PRIMARY KEY (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV)
