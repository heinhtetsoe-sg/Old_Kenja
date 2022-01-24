-- kanji=����
-- $Id: 8212c1db857d855b66555f38436dfab2b3f0f4d4 $

DROP TABLE AFT_DISEASE_ADDITION443_HDAT

CREATE TABLE AFT_DISEASE_ADDITION443_HDAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       SCHOOL_GROUP_TYPE    VARCHAR(2), \
       IDOU_DATE            DATE, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION443_HDAT add constraint PK_AFT_D_ADD443_H \
primary key (EDBOARD_SCHOOLCD, YEAR)