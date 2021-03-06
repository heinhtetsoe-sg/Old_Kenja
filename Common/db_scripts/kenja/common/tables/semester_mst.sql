-- $Id: 9e4f21f497f3eb930b5fce283b3d145c748d76e9 $

DROP TABLE SEMESTER_MST
CREATE TABLE SEMESTER_MST( \
    YEAR         VARCHAR(4)    NOT NULL, \
    SEMESTER     VARCHAR(1)    NOT NULL, \
    SEMESTERNAME VARCHAR(30), \
    SDATE        DATE, \
    EDATE        DATE, \
    REGISTERCD   VARCHAR(8), \
    UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SEMESTER_MST ADD CONSTRAINT PK_SEMESTER_MST PRIMARY KEY (YEAR,SEMESTER)