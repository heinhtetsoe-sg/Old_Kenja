-- kanji=´Á»ú
-- $Id: 145538ec46da422384292574c895cdcdf24df738 $

DROP TABLE AFT_DISEASE_ADDITION443_FIXED_DAT

CREATE TABLE AFT_DISEASE_ADDITION443_FIXED_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       FIXED_DATE           DATE        NOT NULL, \
       COURSECD             VARCHAR(1)  NOT NULL, \
       MAJORCD              VARCHAR(3)  NOT NULL, \
       SCHOOL_GROUP         VARCHAR(2)  NOT NULL, \
       SCHOOL_CD            VARCHAR(8)  NOT NULL, \
       SEQ                  VARCHAR(2)  NOT NULL, \
       INT_VAL1             INTEGER, \
       INT_VAL2             INTEGER, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION443_FIXED_DAT add constraint PK_AFT_D_ADD443_FD \
primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE, COURSECD, MAJORCD, SCHOOL_GROUP, SCHOOL_CD, SEQ)
