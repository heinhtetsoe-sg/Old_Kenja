-- $Id: ae2074370d93eadb8f6e4b57108672e832e1d7a2 $

drop table SCHREG_COMMITTEE_HIST_DAT_OLD
create table SCHREG_COMMITTEE_HIST_DAT_OLD like SCHREG_COMMITTEE_HIST_DAT
insert into SCHREG_COMMITTEE_HIST_DAT_OLD select * from SCHREG_COMMITTEE_HIST_DAT

drop table SCHREG_COMMITTEE_HIST_DAT
CREATE TABLE SCHREG_COMMITTEE_HIST_DAT( \
    SCHOOLCD      VARCHAR(12)   NOT NULL, \
    SCHOOL_KIND   VARCHAR(2)    NOT NULL, \
    YEAR          VARCHAR(4)    NOT NULL, \
    SEQ           INTEGER       NOT NULL, \
    SEMESTER      VARCHAR(1)    NOT NULL, \
    SCHREGNO      VARCHAR(8), \
    GRADE         VARCHAR(2), \
    COMMITTEE_FLG VARCHAR(1), \
    COMMITTEECD   VARCHAR(4), \
    CHARGENAME    VARCHAR(30), \
    EXECUTIVECD   VARCHAR(2), \
    REGISTERCD    VARCHAR(10), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_COMMITTEE_HIST_DAT ADD CONSTRAINT PK_SCH_COMMIT_H_DT PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, YEAR,SEQ)

insert into SCHREG_COMMITTEE_HIST_DAT \
select \
        '000000000000' AS SCHOOLCD, \
        'H' AS SCHOOL_KIND, \
        YEAR, \
        SEQ, \
        SEMESTER, \
        SCHREGNO, \
        GRADE, \
        COMMITTEE_FLG, \
        COMMITTEECD, \
        CHARGENAME, \
        EXECUTIVECD, \
        REGISTERCD, \
        UPDATED \
from SCHREG_COMMITTEE_HIST_DAT_OLD