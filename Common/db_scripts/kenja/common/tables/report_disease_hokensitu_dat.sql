-- kanji=����
-- $Id:

DROP TABLE REPORT_DISEASE_HOKENSITU_DAT

CREATE TABLE REPORT_DISEASE_HOKENSITU_DAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4) NOT NULL, \
       EXECUTE_DATE         DATE NOT NULL, \
       GAITOUNASI_FLG       VARCHAR(1), \
       REGISTERCD           VARCHAR(10), \
       UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table REPORT_DISEASE_HOKENSITU_DAT add constraint PK_MEDEXAM_RHS primary key (EDBOARD_SCHOOLCD, YEAR, EXECUTE_DATE)