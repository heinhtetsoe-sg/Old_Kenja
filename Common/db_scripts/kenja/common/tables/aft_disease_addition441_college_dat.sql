-- kanji=????
-- $Id: 913be7f3c34501b6660b8adf1834e64abf80ff9e $

DROP TABLE AFT_DISEASE_ADDITION441_COLLEGE_DAT

CREATE TABLE AFT_DISEASE_ADDITION441_COLLEGE_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       COURSECD             VARCHAR(1)  NOT NULL, \
       MAJORCD              VARCHAR(3)  NOT NULL, \
       SEX                  VARCHAR(1)  NOT NULL, \
       SCHOOL_SORT          VARCHAR(2)  NOT NULL, \
       SCHOOL_GROUP         VARCHAR(2)  NOT NULL, \
       INT_VAL1             INTEGER, \
       INT_VAL2             INTEGER, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION441_COLLEGE_DAT add constraint PK_AFT_D_441C_D \
primary key (EDBOARD_SCHOOLCD, YEAR, COURSECD, MAJORCD, SEX, SCHOOL_SORT, SCHOOL_GROUP)
