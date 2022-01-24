-- $Id: ab69994a2377957ab2a5d0cb9370b7c1c31a136b $

DROP TABLE PROFICIENCY_COMB_GCALC_EXEC_DAT_OLD
RENAME TABLE PROFICIENCY_COMB_GCALC_EXEC_DAT TO PROFICIENCY_COMB_GCALC_EXEC_DAT_OLD
CREATE TABLE PROFICIENCY_COMB_GCALC_EXEC_DAT( \
    CALC_DATE               date not null, \
    CALC_TIME               time not null, \
    COMBINED_SUBCLASSCD     varchar(6) not null, \
    YEAR                    varchar(4), \
    SEMESTER                varchar(1), \
    PROFICIENCYDIV          varchar(2), \
    PROFICIENCYCD           varchar(4), \
    DIV                     varchar(2), \
    GRADE                   varchar(2), \
    COURSECD                varchar(1), \
    MAJORCD                 varchar(3), \
    COURSECODE              varchar(4), \
    GVAL_CALC               varchar(1), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO PROFICIENCY_COMB_GCALC_EXEC_DAT \
    SELECT \
	    CALC_DATE, \
	    CALC_TIME, \
        COMBINED_SUBCLASSCD, \
	    YEAR, \
	    SEMESTER, \
	    PROFICIENCYDIV, \
	    PROFICIENCYCD, \
	    DIV, \
	    GRADE, \
	    COURSECD, \
	    MAJORCD, \
	    COURSECODE, \
    	GVAL_CALC, \
        REGISTERCD, \
        UPDATED \
    FROM \
        PROFICIENCY_COMB_GCALC_EXEC_DAT_OLD

alter table PROFICIENCY_COMB_GCALC_EXEC_DAT add constraint PK_PRO_COM_G_EXE_D \
        primary key (CALC_DATE, CALC_TIME, COMBINED_SUBCLASSCD)
