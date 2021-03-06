-- $Id: cf5fd1249781eaf5ebe67ef3e797cdcf2e1fd4c5 $

drop table COMMITTEE_YDAT_OLD
create table COMMITTEE_YDAT_OLD like COMMITTEE_YDAT
insert into COMMITTEE_YDAT_OLD select * from COMMITTEE_YDAT

DROP TABLE COMMITTEE_YDAT

CREATE TABLE COMMITTEE_YDAT \
(  \
        "SCHOOLCD"              VARCHAR(12)     NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)      NOT NULL, \
        "YEAR"                  VARCHAR(4)      NOT NULL, \
        "COMMITTEE_FLG"         VARCHAR(1)      NOT NULL, \
        "COMMITTEECD"           VARCHAR(4)      NOT NULL, \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COMMITTEE_YDAT  \
ADD CONSTRAINT PK_COMMITTEE_YDAT  \
PRIMARY KEY  \
(SCHOOLCD, SCHOOL_KIND, YEAR,COMMITTEE_FLG,COMMITTEECD)

insert into COMMITTEE_YDAT \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        YEAR, \
        COMMITTEE_FLG, \
        COMMITTEECD, \
        REGISTERCD, \
        UPDATED \
from COMMITTEE_YDAT_OLD
