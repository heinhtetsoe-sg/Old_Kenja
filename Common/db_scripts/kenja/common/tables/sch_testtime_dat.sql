-- $Id$

DROP TABLE SCH_TESTTIME_DAT
CREATE TABLE SCH_TESTTIME_DAT( \
    YEAR              VARCHAR(4)  NOT NULL, \
    GRADE             VARCHAR(2)  NOT NULL, \
    SEMESTER          VARCHAR(1)  NOT NULL, \
    TESTKINDCD        VARCHAR(2)  NOT NULL, \
    TESTITEMCD        VARCHAR(2)  NOT NULL, \
    PERIODCD          VARCHAR(1)  NOT NULL, \
    STARTTIME_HOUR    VARCHAR(2), \
    STARTTIME_MINUTE  VARCHAR(2), \
    ENDTIME_HOUR      VARCHAR(2), \
    ENDTIME_MINUTE    VARCHAR(2), \
    WORSHIP_FLG       VARCHAR(1), \
    REGISTERCD        VARCHAR(10), \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCH_TESTTIME_DAT ADD CONSTRAINT PK_SCH_TESTTIME_DAT PRIMARY KEY (YEAR, GRADE, SEMESTER, TESTKINDCD, TESTITEMCD, PERIODCD)
