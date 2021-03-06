-- $Id: bd95a34a998ffc20f4414d43ff650e58158c0ad8 $

DROP TABLE SCH_CHR_STF_DIARY_DAT

CREATE TABLE SCH_CHR_STF_DIARY_DAT( \
    SCHOOLCD        VARCHAR(12) NOT NULL, \
    SCHOOL_KIND     VARCHAR(2)  NOT NULL, \
    STAFFCD         VARCHAR(10)  NOT NULL, \
    DIARY_DATE      DATE        NOT NULL, \
    WEATHER         VARCHAR(1)      , \
    WEATHER2        VARCHAR(1)      , \
    TEMPERATURE     DECIMAL(3, 1)   , \
    REMARK          VARCHAR(1600)   , \
    REGISTERCD      VARCHAR(10)     , \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCH_CHR_STF_DIARY_DAT ADD CONSTRAINT PK_SCH_CHR_STF_DIARY_D PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, STAFFCD, DIARY_DATE)
