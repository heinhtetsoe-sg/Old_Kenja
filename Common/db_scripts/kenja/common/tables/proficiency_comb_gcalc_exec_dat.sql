-- $Id: 00a5cf74f0a92b00a81aea2200d0367b97eed68b $

drop table PROFICIENCY_COMB_GCALC_EXEC_DAT

create table PROFICIENCY_COMB_GCALC_EXEC_DAT ( \
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

alter table PROFICIENCY_COMB_GCALC_EXEC_DAT add constraint PK_PRO_COM_G_EXE_D \
        primary key (CALC_DATE, CALC_TIME, COMBINED_SUBCLASSCD)
