-- kanji=????
-- $Id: e5c637cdaa75faca59777e375d646da82228aef9 $

DROP TABLE AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT

CREATE TABLE AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       FIXED_DATE           DATE        NOT NULL, \
       IDOU_DATE            DATE, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION441_COLLEGE_FIXED_HDAT add constraint PK_AFT_D_441C_FH \
primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE)
