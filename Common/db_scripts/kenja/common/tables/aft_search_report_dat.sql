-- kanji=漢字
-- $Id$

DROP TABLE AFT_SEARCH_REPORT_DAT
CREATE TABLE AFT_SEARCH_REPORT_DAT( \
    YEAR                VARCHAR(4)    NOT NULL, \
    DOC_NUMBER          INTEGER       NOT NULL, \
    SUBMISSION_DATE     DATE, \
    NOTICE_TITLE        VARCHAR(100), \
    NOTICE_MESSAGE      VARCHAR(2898), \
    WRITING_DATE        DATE, \
    TRANSMISSION_DATE   DATE, \
    VIEWING_PERIOD_FROM DATE, \
    VIEWING_PERIOD_TO   DATE, \
    REQUEST_ANSWER_FLG  VARCHAR(1), \
    REQUEST_ANSWER_PRG  VARCHAR(20), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE AFT_SEARCH_REPORT_DAT ADD CONSTRAINT PK_AFT_SCH_REP_DAT PRIMARY KEY (YEAR, DOC_NUMBER)