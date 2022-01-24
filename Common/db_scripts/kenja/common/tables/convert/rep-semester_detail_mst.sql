-- $Id: a838e75fb2aa669ef15b1f78bfc414897d772068 $

DROP TABLE SEMESTER_DETAIL_MST_OLD
RENAME TABLE SEMESTER_DETAIL_MST TO SEMESTER_DETAIL_MST_OLD
CREATE TABLE SEMESTER_DETAIL_MST( \
    YEAR            VARCHAR(4)    NOT NULL, \
    SEMESTER        VARCHAR(1)    NOT NULL, \
    SEMESTER_DETAIL VARCHAR(1)    NOT NULL, \
    SEMESTERNAME    VARCHAR(30), \
    SDATE           DATE, \
    EDATE           DATE, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO SEMESTER_DETAIL_MST \
    SELECT \
        YEAR, \
        SEMESTER, \
        SEMESTER_DETAIL, \
        SEMESTERNAME, \
        SDATE, \
        EDATE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SEMESTER_DETAIL_MST_OLD

ALTER TABLE SEMESTER_DETAIL_MST ADD CONSTRAINT PK_SEM_DETAIL_MST PRIMARY KEY (YEAR,SEMESTER,SEMESTER_DETAIL)