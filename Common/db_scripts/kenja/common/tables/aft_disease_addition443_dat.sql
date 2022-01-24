-- kanji=´Á»ú
-- $Id: 553fe66481d42f43e1514ebacb096eb5c9c5c54e $

DROP TABLE AFT_DISEASE_ADDITION443_DAT

CREATE TABLE AFT_DISEASE_ADDITION443_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
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

alter table AFT_DISEASE_ADDITION443_DAT add constraint PK_AFT_D_ADD443_D \
primary key (EDBOARD_SCHOOLCD, YEAR, COURSECD, MAJORCD, SCHOOL_GROUP, SCHOOL_CD, SEQ)
