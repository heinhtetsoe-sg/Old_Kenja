-- $Id: dd87a8a427b13a6f7e4543ec63336fe7e8bbfbcc $

DROP TABLE JOB_YDAT
CREATE TABLE JOB_YDAT( \
    YEAR       VARCHAR(4)    NOT NULL, \
    JOBCD      VARCHAR(4)    NOT NULL, \
    REGISTERCD VARCHAR(8), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JOB_YDAT ADD CONSTRAINT PK_JOB_YDAT PRIMARY KEY (YEAR, JOBCD)