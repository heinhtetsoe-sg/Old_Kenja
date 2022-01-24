-- kanji=����
-- $Id: 7cf7d654c57d010c2bc519aa7833584c1fd1550a $

DROP TABLE AFT_DISEASE_ADDITION441_COLLEGE_HDAT

CREATE TABLE AFT_DISEASE_ADDITION441_COLLEGE_HDAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       IDOU_DATE            DATE, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION441_COLLEGE_HDAT add constraint PK_AFT_D_441C_H \
primary key (EDBOARD_SCHOOLCD, YEAR)
