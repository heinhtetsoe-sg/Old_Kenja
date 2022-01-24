-- $Id: 7a9384008af47bc4a86fe794d35836f3d7c3b5fc $

drop table PROFICIENCY_SUBCLASS_YDAT

create table PROFICIENCY_SUBCLASS_YDAT ( \
    DIV                     varchar(2) not null, \
    GRADE                   varchar(2) not null, \
    COURSECD                varchar(1) not null, \
    MAJORCD                 varchar(3) not null, \
    COURSECODE              varchar(4) not null, \
    CLASSCD                 varchar(2) not null, \
    SCHOOL_KIND             varchar(2) not null, \
    CURRICULUM_CD           varchar(2) not null, \
    SUBCLASSCD              varchar(6) not null, \
    YEAR                    varchar(4) not null, \
    SEMESTER                varchar(1) not null, \
    PROFICIENCYDIV          varchar(2) not null, \
    PROFICIENCYCD           varchar(4) not null, \
    PROFICIENCY_SUBCLASS_CD varchar(6) not null, \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table PROFICIENCY_SUBCLASS_YDAT add constraint PK_PRO_SUBCLASS_Y \
        primary key (DIV, GRADE, COURSECD, MAJORCD, COURSECODE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD)
