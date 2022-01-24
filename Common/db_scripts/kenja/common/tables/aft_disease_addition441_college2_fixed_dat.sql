-- kanji=´Á»ú
-- $Id: 6ef5c3bf6e8ea814f7b64369203f084d4e7b10d2 $

DROP TABLE AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT

CREATE TABLE AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       FIXED_DATE           DATE        NOT NULL, \
       COURSECD             VARCHAR(1)  NOT NULL, \
       MAJORCD              VARCHAR(3)  NOT NULL, \
       SEX                  VARCHAR(1)  NOT NULL, \
       SCHOOL_SORT          VARCHAR(2)  NOT NULL, \
       BUNYA                VARCHAR(2)  NOT NULL, \
       INT_VAL1             INTEGER, \
       INT_VAL2             INTEGER, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION441_COLLEGE2_FIXED_DAT add constraint PK_AFT_D_441C2_FD \
primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE, COURSECD, MAJORCD, SEX, SCHOOL_SORT, BUNYA)
