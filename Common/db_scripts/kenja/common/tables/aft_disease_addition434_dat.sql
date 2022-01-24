-- kanji=´Á»ú
-- $Id: aa65b170dd763b34cf15bc55af5900ebe280ae99 $

DROP TABLE AFT_DISEASE_ADDITION434_DAT

CREATE TABLE AFT_DISEASE_ADDITION434_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       DATA_DIV             VARCHAR(3)  NOT NULL, \
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

alter table AFT_DISEASE_ADDITION434_DAT add constraint PK_AFT_D_ADD434_D \
primary key (EDBOARD_SCHOOLCD, YEAR, DATA_DIV, COURSECD, MAJORCD, AGE, SEX, SEQ, PREF_CD, COMPANY_CD)
