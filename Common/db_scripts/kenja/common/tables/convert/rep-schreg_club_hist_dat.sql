-- $Id: 4d91f80d66dfa93227257915a0edc73d8ff89173 $

drop table SCHREG_CLUB_HIST_DAT_OLD
create table SCHREG_CLUB_HIST_DAT_OLD like SCHREG_CLUB_HIST_DAT
insert into SCHREG_CLUB_HIST_DAT_OLD select * from SCHREG_CLUB_HIST_DAT

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

insert into SCHREG_CLUB_HIST_DAT \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        SCHREGNO, \
        CLUBCD, \
        SDATE, \
        EDATE, \
        EXECUTIVECD, \
        REMARK, \
        REGISTERCD, \
        UPDATED \
from SCHREG_CLUB_HIST_DAT_OLD
