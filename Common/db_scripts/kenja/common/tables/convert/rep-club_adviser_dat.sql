-- $Id: c33e22dced58ccd82709bb0ac0150108e438f163 $

drop table CLUB_ADVISER_DAT_OLD
create table CLUB_ADVISER_DAT_OLD like CLUB_ADVISER_DAT
insert into CLUB_ADVISER_DAT_OLD select * from CLUB_ADVISER_DAT

DROP TABLE CLUB_ADVISER_DAT

CREATE TABLE CLUB_ADVISER_DAT \
(  \
        "SCHOOLCD"              VARCHAR(12)     NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)      NOT NULL, \
        "YEAR"                  VARCHAR(4)      NOT NULL, \
        "CLUBCD"                VARCHAR(4)      NOT NULL, \
        "ADVISER"               VARCHAR(10)      NOT NULL, \
        "CLUBDIV"               VARCHAR(2), \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CLUB_ADVISER_DAT  \
ADD CONSTRAINT PK_CLUB_ADVISER  \
PRIMARY KEY  \
(SCHOOLCD, SCHOOL_KIND, YEAR,CLUBCD,ADVISER)

insert into CLUB_ADVISER_DAT \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        YEAR, \
        CLUBCD, \
        ADVISER, \
        CLUBDIV, \
        REGISTERCD, \
        UPDATED \
from CLUB_ADVISER_DAT_OLD
