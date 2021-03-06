-- $Id: f9dca2cd047509b38321209e630ed6331ae6369d $

DROP TABLE COMMITTEE_ADVISER_DAT

CREATE TABLE COMMITTEE_ADVISER_DAT \
(  \
        "SCHOOLCD"      VARCHAR(12)     NOT NULL, \
        "SCHOOL_KIND"   VARCHAR(2)      NOT NULL, \
        "YEAR"          VARCHAR(4)      NOT NULL, \
        "COMMITTEE_FLG" VARCHAR(1)      NOT NULL, \
        "COMMITTEECD"   VARCHAR(4)      NOT NULL, \
        "ADVISER"       VARCHAR(10)      NOT NULL, \
        "COMMITTEEDIV"  VARCHAR(2), \
        "REGISTERCD"    VARCHAR(10), \
        "UPDATED"       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COMMITTEE_ADVISER_DAT  \
ADD CONSTRAINT PK_COMMITTEE_AD  \
PRIMARY KEY  \
(SCHOOLCD, SCHOOL_KIND, YEAR, COMMITTEE_FLG, COMMITTEECD, ADVISER)
