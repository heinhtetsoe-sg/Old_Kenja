-- kanji=´Á»ú
-- $Id:

DROP TABLE MEDEXAM_DISEASE_HOKENSITU_HDAT

CREATE TABLE MEDEXAM_DISEASE_HOKENSITU_HDAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       SCHREGNO             VARCHAR(8) NOT NULL, \
       AGE                  INT NOT NULL, \
       SEX                  VARCHAR(1) NOT NULL, \
       REGISTERCD           VARCHAR(10), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_HOKENSITU_HDAT add constraint PK_MEDEXAM_HSH primary key (EDBOARD_SCHOOLCD, YEAR, SCHREGNO)
