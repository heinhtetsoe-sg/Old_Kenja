-- kanji=´Á»ú
-- $Id:

DROP TABLE MEDEXAM_DISEASE_HOKENSITU_DAT

CREATE TABLE MEDEXAM_DISEASE_HOKENSITU_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       SCHREGNO             VARCHAR(8) NOT NULL, \
       DATA_DIV             VARCHAR(3) NOT NULL, \
       SEQ                  VARCHAR(2) NOT NULL, \
       INT_VAL              INT, \
       CHAR_VAL             VARCHAR(150), \
       REGISTERCD           VARCHAR(10), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_HOKENSITU_DAT add constraint PK_MEDEXAM_HS primary key (EDBOARD_SCHOOLCD, YEAR, SCHREGNO, DATA_DIV, SEQ)
