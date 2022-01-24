-- kanji=´Á»ú
-- $Id:

DROP TABLE AFT_DISEASE_ADDITION432_DAT

CREATE TABLE AFT_DISEASE_ADDITION432_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       COURSECD             VARCHAR(1) NOT NULL, \
       MAJORCD              VARCHAR(3) NOT NULL, \
       LARGE_DIV            VARCHAR(2) NOT NULL, \
       MIDDLE_DIV           VARCHAR(2) NOT NULL, \
       SMALL_DIV            VARCHAR(3) NOT NULL, \
       SEX                  VARCHAR(1) NOT NULL, \
       COUNT                SMALLINT, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION432_DAT add constraint PK_AFT_D_ADD432_D primary key (EDBOARD_SCHOOLCD, YEAR, COURSECD, MAJORCD, LARGE_DIV, MIDDLE_DIV, SMALL_DIV, SEX)
