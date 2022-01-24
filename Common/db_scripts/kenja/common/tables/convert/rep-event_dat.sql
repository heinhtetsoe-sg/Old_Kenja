-- $Id: ca3941f951705fedbfd4a667c290a3f9c92a336f $

DROP TABLE EVENT_DAT_OLD
RENAME TABLE EVENT_DAT TO EVENT_DAT_OLD
CREATE TABLE EVENT_DAT( \
     GRADE               VARCHAR(2)  NOT NULL, \
     HR_CLASS            VARCHAR(3)  NOT NULL, \
     EXECUTEDATE         DATE        NOT NULL, \
     HR_CLASS_DIV        VARCHAR(1)  NOT NULL, \
     HOLIDAY_FLG         VARCHAR(1), \
     EVENT_ABBV          VARCHAR(9), \
     EVENT_FLG           VARCHAR(1), \
     REMARK1             VARCHAR(150), \
     REMARK2             VARCHAR(150), \
     REGISTERCD          VARCHAR(10), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EVENT_DAT ADD CONSTRAINT PK_EVENT_DAT PRIMARY KEY (GRADE, HR_CLASS, EXECUTEDATE, HR_CLASS_DIV)

INSERT INTO EVENT_DAT \
    SELECT \
         GRADE                                  , \
         HR_CLASS                               , \
         EXECUTEDATE                            , \
         HR_CLASS_DIV                           , \
         HOLIDAY_FLG                            , \
         CAST(NULL AS VARCHAR(9)) AS EVENT_ABBV , \
         EVENT_FLG                              , \
         REMARK1                                , \
         REMARK2                                , \
         REGISTERCD                             , \
         UPDATED                                  \
    FROM \
        EVENT_DAT_OLD

