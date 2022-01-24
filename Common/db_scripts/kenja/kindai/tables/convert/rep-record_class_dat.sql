-- $Id: rep-record_class_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE RECORD_CLASS_DAT_OLD
CREATE TABLE RECORD_CLASS_DAT_OLD LIKE RECORD_CLASS_DAT
INSERT INTO RECORD_CLASS_DAT_OLD SELECT * FROM RECORD_CLASS_DAT

DROP   TABLE RECORD_CLASS_DAT
CREATE TABLE RECORD_CLASS_DAT \
(  \
        "YEAR"                      varchar(4) not null, \
        "CLASSCD"                   varchar(2) not null, \
	    "SCHOOL_KIND"   			varchar(2) not null, \
        "SCHREGNO"                  varchar(8) not null, \
        "SEM1_INTER_REC"            smallint, \
        "SEM1_TERM_REC"             smallint, \
        "SEM1_REC"                  smallint, \
        "SEM2_INTER_REC"            smallint, \
        "SEM2_TERM_REC"             smallint, \
        "SEM2_REC"                  smallint, \
        "SEM3_TERM_REC"             smallint, \
        "SEM3_REC"                  smallint, \
        "GRADE_RECORD"              smallint, \
        "GRADE_ASSESS"              smallint, \
        "GRADE3_RELAASSESS_5STEP"   smallint, \
        "GRADE3_RELAASSESS_10STEP"  smallint, \
        "REGISTERCD"                varchar(8),  \
        "UPDATED"                   timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table RECORD_CLASS_DAT \
add constraint pk_rec_class_dat  \
primary key  \
( \
YEAR, \
CLASSCD, \
SCHOOL_KIND, \
SCHREGNO \
)

INSERT INTO RECORD_CLASS_DAT \
SELECT \
         YEAR                       , \
         CLASSCD                    , \
         SCHOOL_KIND                , \
         SCHREGNO                   , \
         SEM1_INTER_REC             , \
         SEM1_TERM_REC              , \
         SEM1_REC                   , \
         SEM2_INTER_REC             , \
         SEM2_TERM_REC              , \
         SEM2_REC                   , \
         SEM3_TERM_REC              , \
         SEM3_REC                   , \
         GRADE_RECORD               , \
         GRADE_ASSESS               , \
         GRADE3_RELAASSESS_5STEP    , \
         GRADE3_RELAASSESS_10STEP   , \
         REGISTERCD                 , \
         UPDATED                    \
FROM RECORD_CLASS_DAT_OLD \
WHERE CURRICULUM_CD = '2'

