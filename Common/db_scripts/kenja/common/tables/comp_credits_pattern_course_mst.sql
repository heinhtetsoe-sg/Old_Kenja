-- $Id: 8918fea1e7f441b85c05eeb3a3f2c770aa0867cf $

DROP TABLE COMP_CREDITS_PATTERN_COURSE_MST_OLD

RENAME TABLE COMP_CREDITS_PATTERN_COURSE_MST TO COMP_CREDITS_PATTERN_COURSE_MST_OLD

CREATE TABLE COMP_CREDITS_PATTERN_COURSE_MST (          \
   YEAR          VARCHAR(4)   NOT NULL,                 \
   PATTERN_CD    VARCHAR(2)   NOT NULL,                 \
   GRADE         VARCHAR(2)   NOT NULL,                 \
   COURSECD      VARCHAR(1)   NOT NULL,                 \
   MAJORCD       VARCHAR(3)   NOT NULL,                 \
   COURSECODE    VARCHAR(4)   NOT NULL,                 \
   PATTERN_NAME  VARCHAR(90) ,                          \
   REGISTERCD    VARCHAR(10) ,                          \
   UPDATED       TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COMP_CREDITS_PATTERN_COURSE_MST ADD CONSTRAINT PK_COMP_CRDT_P_C_M PRIMARY KEY (YEAR,PATTERN_CD,GRADE,COURSECD,MAJORCD,COURSECODE)
