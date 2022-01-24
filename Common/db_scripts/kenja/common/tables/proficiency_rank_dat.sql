-- $Id: bcdf19b23fd5c28e0b7003c6544db74a35633695 $
drop   table PROFICIENCY_RANK_DAT

create table PROFICIENCY_RANK_DAT ( \
    YEAR                    varchar(4) not null, \
    SEMESTER                varchar(1) not null, \
    PROFICIENCYDIV          varchar(2) not null, \
    PROFICIENCYCD           varchar(4) not null, \
    SCHREGNO                varchar(8) not null, \
    PROFICIENCY_SUBCLASS_CD varchar(6) not null, \
    RANK_DATA_DIV           varchar(2) not null, \
    RANK_DIV                varchar(2) not null, \
    SCORE                   smallint, \
    AVG                     decimal (8,5), \
    RANK                    smallint, \
    DEVIATION               decimal (4,1), \
    REGISTERCD              varchar(8), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PROFICIENCY_RANK_DAT add constraint PK_PRO_RANK_DAT \
      primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, SCHREGNO, PROFICIENCY_SUBCLASS_CD, RANK_DATA_DIV, RANK_DIV)
