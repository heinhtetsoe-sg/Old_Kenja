-- $Id: aebd2e0b71f4e27c7ec1cfe5fb07833ae15640ea $

DROP TABLE COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT
CREATE TABLE COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT( \
    YEAR                    varchar(4)      not null, \
    SCHOOL_CD               varchar(8)      not null, \
    FACULTYCD               varchar(3)      not null, \
    DEPARTMENTCD            varchar(3)      not null, \
    CLASSCD                 varchar(2)      not null, \
    SCHOOL_KIND             varchar(2)      not null, \
    CURRICULUM_CD           varchar(2)      not null, \
    SUBCLASSCD              varchar(6)      not null, \
    GRADE1_FLG              varchar(1)              , \
    GRADE2_FLG              varchar(1)              , \
    GRADE3_FLG              varchar(1)              , \
    COURSECD                varchar(1)              , \
    MAJORCD                 varchar(3)              , \
    COURSECODE              varchar(4)              , \
    REQUIRED_FLG            varchar(1)              , \
    TRANSFER_FLG            varchar(1)              , \
    COMEBACK_FLG            varchar(1)              , \
    REGISTERCD              varchar(10)             , \
    UPDATED                 timestamp default current timestamp \ 
) IN usr1dms index in idx1dms

ALTER TABLE COLLEGE_RECOMMEND_CONDITION_SUBCLASS_DAT \
ADD CONSTRAINT PK_COLLEGE_REC_CON_SUB_DAT PRIMARY KEY (YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
