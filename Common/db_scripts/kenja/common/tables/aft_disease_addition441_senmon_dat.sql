-- kanji=����
-- $Id: 5f7344e879e15560937ff5d37d8f8d367e275045 $

DROP TABLE AFT_DISEASE_ADDITION441_SENMON_DAT

CREATE TABLE AFT_DISEASE_ADDITION441_SENMON_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       COURSECD             VARCHAR(1)  NOT NULL, \
       MAJORCD              VARCHAR(3)  NOT NULL, \
       SEX                  VARCHAR(1)  NOT NULL, \
       SCHOOL_SORT          VARCHAR(2)  NOT NULL, \
       INT_VAL1             INTEGER, \
       INT_VAL2             INTEGER, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION441_SENMON_DAT add constraint PK_AFT_D_441S_D \
primary key (EDBOARD_SCHOOLCD, YEAR, COURSECD, MAJORCD, SEX, SCHOOL_SORT)
