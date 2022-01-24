-- $Id: 515c54b8a7a9a8c75d3275180244f33d08884d4f $

DROP TABLE PROFICIENCY_SUBCLASS_YDAT_OLD
RENAME TABLE PROFICIENCY_SUBCLASS_YDAT TO PROFICIENCY_SUBCLASS_YDAT_OLD
CREATE TABLE PROFICIENCY_SUBCLASS_YDAT( \
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

INSERT INTO PROFICIENCY_SUBCLASS_YDAT \
    SELECT \
        * \
    FROM \
        PROFICIENCY_SUBCLASS_YDAT_OLD

alter table PROFICIENCY_SUBCLASS_YDAT add constraint PK_PRO_SUBCLASS_Y \
        primary key (DIV, GRADE, COURSECD, MAJORCD, COURSECODE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, YEAR, SEMESTER, PROFICIENCYDIV, PROFICIENCYCD, PROFICIENCY_SUBCLASS_CD)
