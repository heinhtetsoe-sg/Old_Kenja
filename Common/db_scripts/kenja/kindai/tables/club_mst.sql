--部クラブマスタ 2004/7/15
DROP TABLE CLUB_MST

CREATE TABLE CLUB_MST \
(  \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "CLUBNAME"              VARCHAR(30), \
        "SDATE"                 DATE, \
        "ACTIVITY_PLACE"        VARCHAR(30), \
        "CLUBROOM_ASSIGN"       VARCHAR(30), \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CLUB_MST  \
ADD CONSTRAINT PK_CLUB_MST  \
PRIMARY KEY  \
(CLUBCD)
