-- $Id: 790b16ff3fafb8f7a5d2f35a5b00ef099f03afc8 $

DROP TABLE PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT_OLD
RENAME TABLE PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT TO PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT_OLD
CREATE TABLE PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT( \
    REPLACECD               varchar(1) not null, \
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
    ATTEND_SUBCLASSCD       varchar(6) not null, \
    WEIGHTING               decimal(5,2), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT \
    SELECT \
        REPLACECD, \
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
        ATTEND_SUBCLASSCD, \
        WEIGHTING, \
        REGISTERCD, \
        UPDATED \
    FROM \
        PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT_OLD

alter table PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT add constraint PK_PRO_SUBCLASS_R \
        primary key (REPLACECD, YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, DIV, GRADE, COURSECD, MAJORCD, COURSECODE, COMBINED_SUBCLASSCD, ATTEND_SUBCLASSCD)
