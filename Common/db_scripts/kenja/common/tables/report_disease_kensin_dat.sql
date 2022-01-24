-- kanji=漢字
-- $Id:

DROP TABLE REPORT_DISEASE_KENSIN_DAT

CREATE TABLE REPORT_DISEASE_KENSIN_DAT \
      ( \
        EDBOARD_SCHOOLCD VARCHAR(12)   NOT NULL, \
        YEAR             VARCHAR(4)    NOT NULL, \
        EXECUTE_DATE     DATE NOT NULL, \
        FIXED_DATE       DATE, \
        REGISTERCD       VARCHAR(10), \
        UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table REPORT_DISEASE_KENSIN_DAT add constraint PK_REPORT_KENSIN primary key (EDBOARD_SCHOOLCD, YEAR, EXECUTE_DATE)
