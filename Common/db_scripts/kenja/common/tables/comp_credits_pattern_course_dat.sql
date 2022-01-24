-- $Id: b802275ceb252d7d2287980416d30096746006f8 $

DROP TABLE COMP_CREDITS_PATTERN_COURSE_DAT_OLD

RENAME TABLE COMP_CREDITS_PATTERN_COURSE_DAT TO COMP_CREDITS_PATTERN_COURSE_DAT_OLD

CREATE TABLE COMP_CREDITS_PATTERN_COURSE_DAT (           \
   YEAR           VARCHAR(4)    NOT NULL,                \
   PATTERN_CD     VARCHAR(2)    NOT NULL,                \
   GRADE          VARCHAR(2)    NOT NULL,                \
   COURSECD       VARCHAR(1)    NOT NULL,                \
   MAJORCD        VARCHAR(3)    NOT NULL,                \
   COURSECODE     VARCHAR(4)    NOT NULL,                \
   CLASSCD        VARCHAR(2)    NOT NULL,                \
   SCHOOL_KIND    VARCHAR(2)    NOT NULL,                \
   CURRICULUM_CD  VARCHAR(2)    NOT NULL,                \
   SUBCLASSCD     VARCHAR(6)    NOT NULL,                \
   COMP_FLG       VARCHAR(1)    ,                        \
   REGISTERCD     VARCHAR(10)   ,                        \
   UPDATED        TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COMP_CREDITS_PATTERN_COURSE_DAT ADD CONSTRAINT PK_COMP_CRDT_P_C_D PRIMARY KEY (YEAR,PATTERN_CD,GRADE,COURSECD,MAJORCD,COURSECODE,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)
