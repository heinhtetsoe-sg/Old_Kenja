-- kanji=漢字
-- $Id:

DROP TABLE MEDEXAM_DISEASE_ADDITION3_COUNT_DAT

CREATE TABLE MEDEXAM_DISEASE_ADDITION3_COUNT_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       SUSPEND_DIRECT_DATE  DATE       NOT NULL, \
       TOTAL_DIV            VARCHAR(1) NOT NULL, \
       DISEASECD            VARCHAR(3) NOT NULL, \
       GRADE                VARCHAR(2) NOT NULL, \
       HR_CLASS             VARCHAR(3) NOT NULL, \
       COUNT                SMALLINT, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION3_COUNT_DAT add constraint PK_MEDEXAM_A3_C_D primary key (EDBOARD_SCHOOLCD, YEAR, SUSPEND_DIRECT_DATE, TOTAL_DIV, DISEASECD, GRADE, HR_CLASS)