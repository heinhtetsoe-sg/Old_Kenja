-- $Id: 4bf14abeceac5f83e374e029e0ac175e50ab7e36 $

DROP TABLE JOB_MST_OLD
RENAME TABLE JOB_MST TO JOB_MST_OLD
CREATE TABLE JOB_MST( \
    JOBCD      VARCHAR(4)    NOT NULL, \
    JOBNAME    VARCHAR(60), \
    BASE_JOBNAME VARCHAR(60), \
    REGISTERCD VARCHAR(8), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO JOB_MST \
    SELECT \
        JOBCD, \
        JOBNAME, \
        CAST(NULL AS VARCHAR(60)) AS BASE_JOBNAME, \
        REGISTERCD, \
        UPDATED \
    FROM \
        JOB_MST_OLD

ALTER TABLE JOB_MST ADD CONSTRAINT PK_JOB_MST PRIMARY KEY (JOBCD)