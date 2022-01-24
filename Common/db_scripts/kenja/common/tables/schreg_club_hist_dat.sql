-- $Id: facbaee4553caf77c47d1eef4e4f0982e3dfd9f4 $

DROP TABLE SCHREG_CLUB_HIST_DAT

CREATE TABLE SCHREG_CLUB_HIST_DAT \
(  \
        "SCHOOLCD"              VARCHAR(12)     NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)      NOT NULL, \
        "SCHREGNO"              VARCHAR(8)      NOT NULL, \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "SDATE"                 DATE            NOT NULL, \
        "EDATE"                 DATE , \
        "EXECUTIVECD"           VARCHAR(2) , \
        "REMARK"                VARCHAR(60) , \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CLUB_HIST_DAT  \
ADD CONSTRAINT PK_SCH_CLUB_H_DAT  \
PRIMARY KEY  \
(SCHOOLCD, SCHOOL_KIND, SCHREGNO,CLUBCD,SDATE)
