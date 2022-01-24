-- $Id: a8a916235bc2c990470c6cdf19921db519572b9e $
drop table PROFICIENCY_SUBCLASS_GROUP_DAT

create table PROFICIENCY_SUBCLASS_GROUP_DAT ( \
    YEAR                    varchar(4) not null, \
    SEMESTER                varchar(1) not null, \
    PROFICIENCYDIV          varchar(2) not null, \
    PROFICIENCYCD           varchar(4) not null, \
    GROUP_DIV               varchar(2) not null, \
    GRADE                   varchar(2) not null, \
    COURSECD                varchar(1) not null, \
    MAJORCD                 varchar(3) not null, \
    COURSECODE              varchar(4) not null, \
    PROFICIENCY_SUBCLASS_CD varchar(6) not null, \
    REGISTERCD              varchar(8) , \
    UPDATED                 timestamp default current timestamp \
)  in usr1dms index in idx1dms

alter table PROFICIENCY_SUBCLASS_GROUP_DAT add constraint PK_PRO_SUB_GP_DAT \
primary key (YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, GROUP_DIV, GRADE, COURSECD, MAJORCD, COURSECODE, PROFICIENCY_SUBCLASS_CD)

