-- kanji=漢字
-- $Id:

DROP TABLE REPORT_DISEASE_ADDITION4_DAT

CREATE TABLE REPORT_DISEASE_ADDITION4_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       DISEASECD            VARCHAR(3) NOT NULL, \
       GRADE                VARCHAR(2) NOT NULL, \
       HR_CLASS             VARCHAR(3) NOT NULL, \
       ACTION_S_DATE        DATE NOT NULL, \
       EXECUTE_DATE         DATE, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table REPORT_DISEASE_ADDITION4_DAT add constraint PK_REPORT_D_A4_D primary key (EDBOARD_SCHOOLCD, YEAR, DISEASECD, GRADE, HR_CLASS, ACTION_S_DATE)
