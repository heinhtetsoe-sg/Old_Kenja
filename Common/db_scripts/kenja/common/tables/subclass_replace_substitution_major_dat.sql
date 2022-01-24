-- $Id: 1b1d9714a5940c769a3b7dee62dcb0cb87621d3c $

DROP TABLE SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT
CREATE TABLE SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT( \
    YEAR                        VARCHAR(4) NOT NULL, \
    SUBSTITUTION_CLASSCD        VARCHAR(2) NOT NULL, \
    SUBSTITUTION_SCHOOL_KIND    VARCHAR(2) NOT NULL, \
    SUBSTITUTION_CURRICULUM_CD  VARCHAR(2) NOT NULL, \
    SUBSTITUTION_SUBCLASSCD     VARCHAR(6) NOT NULL, \
    ATTEND_CLASSCD              VARCHAR(2) NOT NULL, \
    ATTEND_SCHOOL_KIND          VARCHAR(2) NOT NULL, \
    ATTEND_CURRICULUM_CD        VARCHAR(2) NOT NULL, \
    ATTEND_SUBCLASSCD           VARCHAR(6) NOT NULL, \
    GRADE                       VARCHAR(2) NOT NULL, \
    COURSECD                    VARCHAR(1) NOT NULL, \
    MAJORCD                     VARCHAR(3) NOT NULL, \
    COURSECODE                  VARCHAR(4) NOT NULL, \
    REGISTERCD                  VARCHAR(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SUBCLASS_REPLACE_SUBSTITUTION_MAJOR_DAT ADD CONSTRAINT SUBCLASS_R_S_M_DAT PRIMARY KEY (YEAR, SUBSTITUTION_CLASSCD, SUBSTITUTION_SCHOOL_KIND, SUBSTITUTION_CURRICULUM_CD, SUBSTITUTION_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD, GRADE, COURSECD, MAJORCD, COURSECODE)