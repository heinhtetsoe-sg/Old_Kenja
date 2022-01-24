-- $Id: record_class_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

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

