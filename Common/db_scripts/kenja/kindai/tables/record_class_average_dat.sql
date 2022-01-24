-- $Id: record_class_average_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP   TABLE RECORD_CLASS_AVERAGE_DAT
CREATE TABLE RECORD_CLASS_AVERAGE_DAT \
(  \
        "YEAR"                      varchar(4) not null, \
        "GRADE"                     varchar(2) not null, \
        "HR_CLASS"                  varchar(3) not null, \
        "CLASSCD"                   varchar(2) not null, \
	    "SCHOOL_KIND"   			varchar(2) not null, \
        "CALC_DIV"                  varchar(1) not null, \
        "SEM1_INTER_REC"            decimal(6,1), \
        "SEM1_TERM_REC"             decimal(6,1), \
        "SEM1_REC"                  decimal(6,1), \
        "SEM2_INTER_REC"            decimal(6,1), \
        "SEM2_TERM_REC"             decimal(6,1), \
        "SEM2_REC"                  decimal(6,1), \
        "SEM3_TERM_REC"             decimal(6,1), \
        "SEM3_REC"                  decimal(6,1), \
        "GRADE_RECORD"              decimal(6,1), \
        "GRADE_ASSESS"              decimal(6,1), \
        "GRADE3_RELAASSESS_5STEP"   decimal(6,1), \
        "GRADE3_RELAASSESS_10STEP"  decimal(6,1), \
        "REGISTERCD"                varchar(8),  \
        "UPDATED"                   timestamp default current timestamp  \
) in usr1dms index in idx1dms

alter table RECORD_CLASS_AVERAGE_DAT \
add constraint pk_rec_class_avg  \
primary key  \
( \
YEAR,GRADE,HR_CLASS,CLASSCD,SCHOOL_KIND,CALC_DIV \
)

