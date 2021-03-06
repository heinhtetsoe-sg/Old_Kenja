-- $Id: 393215bb81763f817928e1bd5f06084551cff299 $

DROP TABLE EDBOARD_MAJOR_YDAT
CREATE TABLE EDBOARD_MAJOR_YDAT( \
    EDBOARD_SCHOOLCD    VARCHAR(12)     NOT NULL, \
    YEAR                VARCHAR(4)      NOT NULL, \
    COURSECD            VARCHAR(1)      NOT NULL, \
    MAJORCD             VARCHAR(3)      NOT NULL, \
    SCHOOLDIV           VARCHAR(1), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EDBOARD_MAJOR_YDAT ADD CONSTRAINT \
PK_ED_MAJOR_YDAT PRIMARY KEY (EDBOARD_SCHOOLCD, YEAR, COURSECD, MAJORCD)
