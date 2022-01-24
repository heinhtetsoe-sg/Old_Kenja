-- $Id: ebee02d972e49baf7b8546d40674d2e0081323d4 $

DROP TABLE PROFICIENCY_COMB_GCALC_DAT_OLD
RENAME TABLE PROFICIENCY_COMB_GCALC_DAT TO PROFICIENCY_COMB_GCALC_DAT_OLD
CREATE TABLE PROFICIENCY_COMB_GCALC_DAT( \
    YEAR                    varchar(4) not null, \
    SEMESTER                varchar(1) not null, \
    PROFICIENCYDIV          varchar(2) not null, \
    PROFICIENCYCD           varchar(4) not null, \
    DIV                     varchar(2) not null, \
    GRADE                   varchar(2) not null, \
    COURSECD                varchar(1) not null, \
    MAJORCD                 varchar(3) not null, \
    COURSECODE              varchar(4) not null, \
    COMBINED_SUBCLASSCD     varchar(6) not null, \
    GVAL_CALC               varchar(1), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO PROFICIENCY_COMB_GCALC_DAT \
    SELECT \
        YEAR, \
        SEMESTER, \
        PROFICIENCYDIV, \
        PROFICIENCYCD, \
        DIV, \
        GRADE, \
        COURSECD, \
        MAJORCD, \
        COURSECODE, \
        COMBINED_SUBCLASSCD, \
        GVAL_CALC, \
        REGISTERCD, \
        UPDATED \
    FROM \
        PROFICIENCY_COMB_GCALC_DAT_OLD

alter table PROFICIENCY_COMB_GCALC_DAT add constraint PK_PRO_COM_G_D \
        primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, DIV, GRADE, COURSECD, MAJORCD, COURSECODE, COMBINED_SUBCLASSCD)
