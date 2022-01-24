-- $Id: 486d96e0387343f54633ecbcdf08a8221553d880 $

DROP TABLE COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT
CREATE TABLE COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT( \
    YEAR                    varchar(4)      not null, \
    SCHOOL_CD               varchar(8)      not null, \
    FACULTYCD               varchar(3)      not null, \
    DEPARTMENTCD            varchar(3)      not null, \
    QUALIFIED_CD            varchar(4)      not null, \
    VALID_S_DATE            date                    , \
    CONDITION_RANK          varchar(3)              , \
    CONDITION_SCORE         decimal(4,1)            , \
    REGISTERCD              varchar(10)             , \
    UPDATED                 timestamp default current timestamp \ 
) IN usr1dms index in idx1dms

ALTER TABLE COLLEGE_RECOMMEND_CONDITION_QUALIFIED_DAT \
ADD CONSTRAINT PK_COLLEGE_REC_CON_QUAL_DAT PRIMARY KEY (YEAR, SCHOOL_CD, FACULTYCD, DEPARTMENTCD, QUALIFIED_CD)