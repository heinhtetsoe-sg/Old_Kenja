-- kanji=´Á»ú
-- $Id: 9b6bf478cc9896c401293e9702da81c38ce8e74b $

DROP TABLE AFT_DISEASE_ADDITION441_SENMON2_DAT

CREATE TABLE AFT_DISEASE_ADDITION441_SENMON2_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       COURSECD             VARCHAR(1)  NOT NULL, \
       MAJORCD              VARCHAR(3)  NOT NULL, \
       SEX                  VARCHAR(1)  NOT NULL, \
       BUNYA                VARCHAR(2)  NOT NULL, \
       INT_VAL1             INTEGER, \
       INT_VAL2             INTEGER, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION441_SENMON2_DAT add constraint PK_AFT_D_441S2_D \
primary key (EDBOARD_SCHOOLCD, YEAR, COURSECD, MAJORCD, SEX, BUNYA)
