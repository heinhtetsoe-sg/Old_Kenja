-- kanji=´Á»ú
-- $Id: e27a9e30996be87fe7edcf94b9dcc8dd946a62af $

DROP TABLE AFT_DISEASE_ADDITION434_FIXED_DAT

CREATE TABLE AFT_DISEASE_ADDITION434_FIXED_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       DATA_DIV             VARCHAR(3)  NOT NULL, \
       FIXED_DATE           DATE        NOT NULL, \
       COURSECD             VARCHAR(1)  NOT NULL, \
       MAJORCD              VARCHAR(3)  NOT NULL, \
       AGE                  INTEGER     NOT NULL, \
       SEX                  VARCHAR(1)  NOT NULL, \
       SEQ                  VARCHAR(2)  NOT NULL, \
       PREF_CD              VARCHAR(2)  NOT NULL, \
       COMPANY_CD           VARCHAR(8)  NOT NULL, \
       INT_VAL1             INTEGER, \
       INT_VAL2             INTEGER, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION434_FIXED_DAT add constraint PK_AFT_D_ADD434_FD \
primary key (EDBOARD_SCHOOLCD, YEAR, DATA_DIV, FIXED_DATE, COURSECD, MAJORCD, AGE, SEX, SEQ, PREF_CD, COMPANY_CD)
