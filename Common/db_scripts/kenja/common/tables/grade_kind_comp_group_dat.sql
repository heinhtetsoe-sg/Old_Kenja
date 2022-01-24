-- $Id: caade6cb2c6f76a9d0492a59a33c73b6dc30f0ea $

DROP TABLE GRADE_KIND_COMP_GROUP_DAT

CREATE TABLE GRADE_KIND_COMP_GROUP_DAT( \
    YEAR                VARCHAR(4) NOT NULL, \
    SEMESTER            VARCHAR(1) NOT NULL, \
    GAKUBU_SCHOOL_KIND  VARCHAR(2) NOT NULL, \
    GHR_CD              VARCHAR(2) NOT NULL, \
    GRADE               VARCHAR(2) NOT NULL, \
    HR_CLASS            VARCHAR(3) NOT NULL, \
    CONDITION           VARCHAR(1) NOT NULL, \
    GROUPCD             VARCHAR(4) NOT NULL, \
    CLASSCD             VARCHAR(2) NOT NULL, \
    SCHOOL_KIND         VARCHAR(2) NOT NULL, \
    CURRICULUM_CD       VARCHAR(2) NOT NULL, \
    SUBCLASSCD          VARCHAR(6) NOT NULL, \
    UNIT_AIM_DIV        VARCHAR(1), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GRADE_KIND_COMP_GROUP_DAT ADD CONSTRAINT PK_GK_COMP_GP_DAT PRIMARY KEY (YEAR, SEMESTER, GAKUBU_SCHOOL_KIND, GHR_CD, GRADE, HR_CLASS, CONDITION, GROUPCD, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)
