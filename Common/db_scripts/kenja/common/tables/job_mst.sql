-- $Id: 8f3950eecde72e7deb13d2f2bec1fd55334cd47f $

DROP TABLE JOB_MST
CREATE TABLE JOB_MST( \
    JOBCD        VARCHAR(4)    NOT NULL, \
    JOBNAME      VARCHAR(60), \
    BASE_JOBNAME VARCHAR(60), \
    REGISTERCD VARCHAR(8), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JOB_MST ADD CONSTRAINT PK_JOB_MST PRIMARY KEY (JOBCD)