-- kanji=´Á»ú
-- $Id:

DROP TABLE MEDEXAM_CARE_DAT

CREATE TABLE MEDEXAM_CARE_DAT \
      (YEAR                 VARCHAR(4) NOT NULL, \
       SCHREGNO             VARCHAR(8) NOT NULL, \
       CARE_DIV             VARCHAR(2) NOT NULL, \
       CARE_KIND            VARCHAR(2) NOT NULL, \
       CARE_ITEM            VARCHAR(2) NOT NULL, \
       CARE_SEQ             VARCHAR(2) NOT NULL, \
       CARE_REMARK1         VARCHAR(1000), \
       CARE_REMARK2         VARCHAR(150), \
       REGISTERCD           VARCHAR(10), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_CARE_DAT add constraint PK_MEDEXAM_C_D primary key (YEAR, SCHREGNO, CARE_DIV, CARE_KIND, CARE_ITEM, CARE_SEQ)
