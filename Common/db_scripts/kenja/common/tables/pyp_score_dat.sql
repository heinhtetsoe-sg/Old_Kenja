-- $Id: ea65fa67ef4ceca5b534cfa6dba4be382819e615 $

DROP TABLE PYP_SCORE_DAT
CREATE TABLE PYP_SCORE_DAT( \
    YEAR                    VARCHAR(4)    NOT NULL, \
    SEMESTER                VARCHAR(1)    NOT NULL, \
    TESTKINDCD              VARCHAR(2)    NOT NULL, \
    TESTITEMCD              VARCHAR(2)    NOT NULL, \
    SCORE_DIV               VARCHAR(2)    NOT NULL, \
    CLASSCD                 VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND             VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD           VARCHAR(2)    NOT NULL, \
    SUBCLASSCD              VARCHAR(6)    NOT NULL, \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    OUTPUTS_SCORE           SMALLINT              , \
    SKILLS_SCORE            SMALLINT              , \
    TOTAL_SCORE             DECIMAL(2, 1)         , \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN usr1dms index in idx1dms

ALTER TABLE PYP_SCORE_DAT ADD CONSTRAINT PK_PYP_SCORE_DAT PRIMARY KEY (YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)
