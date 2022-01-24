-- $Id: 1ed910e62c6ed3d4219268bdd3dc423ee0dc1b12 $

DROP TABLE COLLEGE_RECOMMEND_CONDITION_MAIN_DAT
CREATE TABLE COLLEGE_RECOMMEND_CONDITION_MAIN_DAT( \
    YEAR                        varchar(4)      not null, \
    SCHOOL_CD                   varchar(8)      not null, \
    FACULTYCD                   varchar(3)      not null, \
    DEPARTMENTCD                varchar(3)      not null, \
    COURSE_CONDITION_FLG        varchar(1)              , \
    SUBCLASS_CONDITION_FLG      varchar(1)              , \
    QUALIFIED_CONDITION_FLG     varchar(1)              , \
    SUBCLASS_NUM                varchar(1)              , \
    REGISTERCD                  varchar(10)             , \
    UPDATED                     timestamp default current timestamp \ 
) IN usr1dms index in idx1dms

ALTER TABLE COLLEGE_RECOMMEND_CONDITION_MAIN_DAT \
ADD CONSTRAINT PK_COLLEGE_REC_CON_MAIN_DAT PRIMARY KEY (YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD)
