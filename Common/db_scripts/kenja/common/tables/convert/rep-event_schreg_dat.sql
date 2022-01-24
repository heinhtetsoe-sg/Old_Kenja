-- $Id: 60773c9580960409d03bed91084942301a8abfe8 $

DROP TABLE EVENT_SCHREG_DAT_OLD
RENAME TABLE EVENT_SCHREG_DAT TO EVENT_SCHREG_DAT_OLD
CREATE TABLE EVENT_SCHREG_DAT( \
     SCHREGNO            VARCHAR(8)  NOT NULL, \
     EXECUTEDATE         DATE        NOT NULL, \
     HOLIDAY_FLG         VARCHAR(1), \
     EVENT_ABBV          VARCHAR(9), \
     EVENT_FLG           VARCHAR(1), \
     REMARK1             VARCHAR(150), \
     REMARK2             VARCHAR(150), \
     REGISTERCD          VARCHAR(10), \
     UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EVENT_SCHREG_DAT ADD CONSTRAINT PK_EVENT_SCH_DAT PRIMARY KEY (SCHREGNO, EXECUTEDATE)

INSERT INTO EVENT_SCHREG_DAT \
    SELECT \
     SCHREGNO                                , \
     EXECUTEDATE                             , \
     HOLIDAY_FLG                             , \
     CAST(NULL AS VARCHAR(9)) AS EVENT_ABBV  , \
     EVENT_FLG                               , \
     REMARK1                                 , \
     REMARK2                                 , \
     REGISTERCD                              , \
     UPDATED                                   \
    FROM \
        EVENT_SCHREG_DAT_OLD

