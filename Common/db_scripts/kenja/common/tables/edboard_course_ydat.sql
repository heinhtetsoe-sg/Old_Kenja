-- $Id: 7c93ae4b4df1b2b98bd84acc0ec6668ce8914969 $

DROP TABLE EDBOARD_COURSE_YDAT
CREATE TABLE EDBOARD_COURSE_YDAT( \
    EDBOARD_SCHOOLCD    VARCHAR(12)     NOT NULL, \
    YEAR                VARCHAR(4)      NOT NULL, \
    COURSECD            VARCHAR(1)      NOT NULL, \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EDBOARD_COURSE_YDAT ADD CONSTRAINT \
PK_ED_COURSE_YDAT PRIMARY KEY (EDBOARD_SCHOOLCD, YEAR, COURSECD)
