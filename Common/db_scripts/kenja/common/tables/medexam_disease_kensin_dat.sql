-- kanji=����
-- $Id:

DROP TABLE MEDEXAM_DISEASE_KENSIN_DAT

CREATE TABLE MEDEXAM_DISEASE_KENSIN_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       AGE                  INT NOT NULL, \
       SEX                  VARCHAR(1) NOT NULL, \
       DATA_DIV             VARCHAR(3) NOT NULL, \
       SEQ                  VARCHAR(2) NOT NULL, \
       INT_VAL              INT, \
       CHAR_VAL             VARCHAR(150), \
       REGISTERCD           VARCHAR(10), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_KENSIN_DAT add constraint PK_MEDEXAM_KS primary key (EDBOARD_SCHOOLCD, YEAR, AGE, SEX, DATA_DIV, SEQ)
