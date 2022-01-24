-- $Id: ba1e74c4969fd24abf64bff3cc0a9a4ee955ca41 $

DROP TABLE POSITION_YDAT
CREATE TABLE POSITION_YDAT( \
    YEAR       VARCHAR(4)    NOT NULL, \
    POSITIONCD VARCHAR(4)    NOT NULL, \
    REGISTERCD VARCHAR(8), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE POSITION_YDAT ADD CONSTRAINT PK_POSITION_YDAT PRIMARY KEY (YEAR, POSITIONCD)