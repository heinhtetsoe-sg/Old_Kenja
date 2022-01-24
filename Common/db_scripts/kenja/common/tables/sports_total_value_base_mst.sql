-- $Id: 63a458054970d34303b46091c951a11df38d9db6 $

DROP TABLE SPORTS_TOTAL_VALUE_BASE_MST
CREATE TABLE SPORTS_TOTAL_VALUE_BASE_MST ( \
    YEAR                  VARCHAR(4)    NOT NULL, \
    AGE                   varchar(2)    NOT NULL, \
    SEX                   VARCHAR(1)    NOT NULL, \
    TOTAL_LEVEL           SMALLINT      NOT NULL, \
    TOTAL_MARK            VARCHAR(6)            , \
    TOTAL_SCORE_LOW       SMALLINT              , \
    TOTAL_SCORE_HIGH      SMALLINT              , \
    REGISTERCD            VARCHAR(10)           , \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SPORTS_TOTAL_VALUE_BASE_MST ADD CONSTRAINT PK_SPORTS_SC_INC_ADD_DAT PRIMARY KEY (YEAR, AGE, SEX, TOTAL_LEVEL)
