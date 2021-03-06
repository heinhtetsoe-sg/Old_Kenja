-- $ID: FINSCHOOL_YDAT.SQL 56577 2017-10-22 11:35:50Z MAESHIRO $
DROP TABLE PARTNER_SCHOOL_YDAT

CREATE TABLE PARTNER_SCHOOL_YDAT ( \
     YEAR                   VARCHAR(4) NOT NULL, \
     PARTNER_SCHOOLCD       VARCHAR(12) NOT NULL, \
     REGISTERCD             VARCHAR(10), \
     UPDATED                TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE PARTNER_SCHOOL_YDAT ADD CONSTRAINT PK_PARTNER_SCH_YDAT PRIMARY KEY \
    (YEAR, PARTNER_SCHOOLCD)
