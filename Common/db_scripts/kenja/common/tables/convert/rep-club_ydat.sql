-- $Id: c41e974dd0f7f5fb65222b596d758276627c5468 $

drop table CLUB_YDAT_OLD
create table CLUB_YDAT_OLD like CLUB_YDAT
insert into CLUB_YDAT_OLD select * from CLUB_YDAT

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

insert into CLUB_YDAT \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        YEAR, \
        CLUBCD, \
        REGISTERCD, \
        UPDATED \
from CLUB_YDAT_OLD
