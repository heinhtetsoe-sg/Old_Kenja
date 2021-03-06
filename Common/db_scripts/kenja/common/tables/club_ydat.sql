-- $Id: 3e14233eba93f1b0f8c7a3557054c799784fc3dc $

DROP TABLE CLUB_YDAT

CREATE TABLE CLUB_YDAT \
(  \
        "SCHOOLCD"              VARCHAR(12)     NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)      NOT NULL, \
        "YEAR"                  VARCHAR(4)      NOT NULL, \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CLUB_YDAT  \
ADD CONSTRAINT PK_CLUB_YDAT  \
PRIMARY KEY  \
(SCHOOLCD, SCHOOL_KIND, YEAR,CLUBCD)
