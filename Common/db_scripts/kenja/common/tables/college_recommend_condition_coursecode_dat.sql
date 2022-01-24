-- $Id: e8545fe6c67b446bf6aef527bf0887a781cf0341 $

DROP TABLE COLLEGE_RECOMMEND_CONDITION_COURSE_DAT
CREATE TABLE COLLEGE_RECOMMEND_CONDITION_COURSE_DAT( \
    YEAR                    varchar(4)      not null, \
    SCHOOL_CD               varchar(8)      not null, \
    FACULTYCD               varchar(3)      not null, \
    DEPARTMENTCD            varchar(3)      not null, \
    COURSECD                varchar(1)      not null, \
    MAJORCD                 varchar(3)      not null, \
    COURSECODE              varchar(4)      not null, \
    REGISTERCD              varchar(10)             , \
    UPDATED                 timestamp default current timestamp \ 
) IN usr1dms index in idx1dms

ALTER TABLE COLLEGE_RECOMMEND_CONDITION_COURSE_DAT \
ADD CONSTRAINT PK_COLLEGE_REC_CON_COURSE_DAT PRIMARY KEY (YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, COURSECD, MAJORCD, COURSECODE)
