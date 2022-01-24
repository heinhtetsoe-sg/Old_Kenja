-- $Id: 741f485cec8b145a89cb2b12f7f88fdcc7a14767 $

drop table CLUB_MST_OLD
create table CLUB_MST_OLD like CLUB_MST
insert into CLUB_MST_OLD select * from CLUB_MST

drop table CLUB_MST
CREATE TABLE CLUB_MST \
(  \
        "SCHOOLCD"              VARCHAR(12)     NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)      NOT NULL, \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "CLUBNAME"              VARCHAR(60), \
        "SDATE"                 DATE, \
        "ACTIVITY_PLACE"        VARCHAR(30), \
        "CLUBROOM_ASSIGN"       VARCHAR(30), \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CLUB_MST ADD CONSTRAINT PK_CLUB_MST2 PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, CLUBCD)

insert into CLUB_MST \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        CLUBCD, \
        CLUBNAME, \
        SDATE, \
        ACTIVITY_PLACE, \
        CLUBROOM_ASSIGN, \
        REGISTERCD, \
        UPDATED \
from CLUB_MST_OLD
