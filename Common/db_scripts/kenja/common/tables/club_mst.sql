-- $Id: 7ce6a2120713b5fb6579b7685dfb0d213b293385 $

DROP TABLE CLUB_MST

CREATE TABLE CLUB_MST \
(  \
        "SCHOOLCD"              VARCHAR(12)      NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)      NOT NULL, \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "CLUBNAME"              VARCHAR(60), \
        "SDATE"                 DATE, \
        "ACTIVITY_PLACE"        VARCHAR(30), \
        "CLUBROOM_ASSIGN"       VARCHAR(30), \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CLUB_MST  \
ADD CONSTRAINT PK_CLUB_MST  \
PRIMARY KEY  \
(SCHOOLCD, SCHOOL_KIND, CLUBCD)