-- $Id: 7e69b97ab321e6fe9ec08fecec6a1f700f888115 $

drop table PROFICIENCY_COMB_GCALC_DAT

create table PROFICIENCY_COMB_GCALC_DAT ( \
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

alter table PROFICIENCY_COMB_GCALC_DAT add constraint PK_PRO_COM_G_D \
        primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, DIV, GRADE, COURSECD, MAJORCD, COURSECODE, COMBINED_SUBCLASSCD)
