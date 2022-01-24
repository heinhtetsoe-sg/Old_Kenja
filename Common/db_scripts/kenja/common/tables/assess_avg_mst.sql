-- kanji=漢字
-- $Id: 1ad3d9bb1925de930d331dfb6c0bff3e5f534896 $

DROP TABLE ASSESS_AVG_MST

CREATE TABLE ASSESS_AVG_MST \
(  \
    YEAR            VARCHAR(4) NOT NULL, \
    ASSESSAVG       SMALLINT NOT NULL, \
    ASSESSLEVEL     SMALLINT NOT NULL, \
    ASSESSMARK      VARCHAR(6), \
    ASSESSLOW       DECIMAL(4,1), \
    ASSESSHIGH      DECIMAL(4,1), \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ASSESS_AVG_MST ADD CONSTRAINT PK_ASSESS_AVG_MST \
PRIMARY KEY (YEAR, ASSESSAVG, ASSESSLEVEL)