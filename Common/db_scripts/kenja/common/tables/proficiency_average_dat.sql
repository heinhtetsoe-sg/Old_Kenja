-- $Id: 9b9902e73918e8cc1079820bd8f94e7fbf601c60 $
drop   table PROFICIENCY_AVERAGE_DAT

create table PROFICIENCY_AVERAGE_DAT ( \
    YEAR                    varchar(4) not null, \
    SEMESTER                varchar(1) not null, \
    PROFICIENCYDIV          varchar(2) not null, \
    PROFICIENCYCD           varchar(4) not null, \
    PROFICIENCY_SUBCLASS_CD varchar(6) not null, \
    DATA_DIV                varchar(1) not null, \
    AVG_DIV                 varchar(2) not null, \
    GRADE                   varchar(2) not null, \
    HR_CLASS                varchar(3) not null, \
    COURSECD                varchar(1) not null, \
    MAJORCD                 varchar(3) not null, \
    COURSECODE              varchar(4) not null, \
    SCORE                   integer, \
    SCORE_KANSAN            integer, \
    HIGHSCORE               integer, \
    HIGHSCORE_KANSAN        integer, \
    LOWSCORE                integer, \
    LOWSCORE_KANSAN         integer, \
    COUNT                   smallint, \
    AVG                     decimal (9,5), \
    AVG_KANSAN              decimal (9,5), \
    STDDEV                  decimal (5,1), \
    STDDEV_KANSAN           decimal (5,1), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PROFICIENCY_AVERAGE_DAT add constraint PK_PRO_AVERAGE_D \
      primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD, DATA_DIV, AVG_DIV, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE)
