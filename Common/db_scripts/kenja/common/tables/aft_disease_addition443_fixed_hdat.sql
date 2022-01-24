-- kanji=����
-- $Id: 3a3664fe8dd227bc325397316c45e3e5180aadbf $

DROP TABLE AFT_DISEASE_ADDITION443_FIXED_HDAT

CREATE TABLE AFT_DISEASE_ADDITION443_FIXED_HDAT \
      (EDBOARD_SCHOOLCD     VARCHAR(12) NOT NULL, \
       YEAR                 VARCHAR(4)  NOT NULL, \
       FIXED_DATE           DATE        NOT NULL, \
       SCHOOL_GROUP_TYPE    VARCHAR(2), \
       IDOU_DATE            DATE, \
       REGISTERCD           VARCHAR(10), \
       UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table AFT_DISEASE_ADDITION443_FIXED_HDAT add constraint PK_AFT_D_ADD443_FH \
primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE)