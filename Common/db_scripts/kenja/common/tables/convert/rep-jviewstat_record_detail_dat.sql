-- kanji=����
-- $Id: d921a022f4ffd35ea01c2db522cf6d462884e483 $

drop table JVIEWSTAT_RECORD_DETAIL_DAT_OLD
rename table JVIEWSTAT_RECORD_DETAIL_DAT TO JVIEWSTAT_RECORD_DETAIL_DAT_OLD

CREATE TABLE JVIEWSTAT_RECORD_DETAIL_DAT(  \
    YEAR          VARCHAR(4)  NOT NULL, \
    SEMESTER      VARCHAR(1)  NOT NULL, \
    SCHREGNO      VARCHAR(8)  NOT NULL, \
    CLASSCD       VARCHAR(2)  NOT NULL, \
    SCHOOL_KIND   VARCHAR(2)  NOT NULL, \
    CURRICULUM_CD VARCHAR(2)  NOT NULL, \
    SUBCLASSCD    VARCHAR(6)  NOT NULL, \
    VIEWCD        VARCHAR(4)  NOT NULL, \
    REMARK1       SMALLINT,  \
    REMARK2       SMALLINT,  \
    REMARK3       DECIMAL(4, 1),  \
    REMARK4       DECIMAL(5, 2),  \
    REMARK5       DECIMAL(5, 2),  \
    REGISTERCD    VARCHAR(10),  \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

insert into JVIEWSTAT_RECORD_DETAIL_DAT \
    select \
        YEAR, \
        SEMESTER, \
        SCHREGNO, \
        CLASSCD, \
        SCHOOL_KIND, \
        CURRICULUM_CD, \
        SUBCLASSCD, \
        VIEWCD, \
        REMARK1, \
        REMARK2, \
        REMARK3, \
        cast(null as DECIMAL(5, 2)) AS REMARK4, \
        cast(null as DECIMAL(5, 2)) AS REMARK5, \
        REGISTERCD, \
        UPDATED \
     from \
         JVIEWSTAT_RECORD_DETAIL_DAT_OLD

alter table JVIEWSTAT_RECORD_DETAIL_DAT ADD CONSTRAINT PK_JVIEWSTAT_REC_D PRIMARY KEY (YEAR, SEMESTER, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, VIEWCD)
