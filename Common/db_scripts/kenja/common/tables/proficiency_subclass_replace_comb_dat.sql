-- $Id: 232cf375af06f7596dc49c6b9960616cb0f06daf $

drop table PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT

create table PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT ( \
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

alter table PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT add constraint PK_PRO_SUBCLASS_R \
        primary key (REPLACECD, YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, DIV, GRADE, COURSECD, MAJORCD, COURSECODE, COMBINED_SUBCLASSCD, ATTEND_SUBCLASSCD)
