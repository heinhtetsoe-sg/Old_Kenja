-- $Id: 1ef8770b21e4915dd710683c34f2ee8b0068af14 $

DROP TABLE SPORTS_SCORE_ADDITION521_DAT
CREATE TABLE SPORTS_SCORE_ADDITION521_DAT ( \
    EDBOARD_SCHOOLCD  VARCHAR(12)   NOT NULL, \
    YEAR              VARCHAR(4)    NOT NULL, \
    GRADE             VARCHAR(2)    NOT NULL, \
    COURSECD          VARCHAR(1)    NOT NULL, \
    MAJORCD           VARCHAR(3)    NOT NULL, \
    SEX               VARCHAR(1)    NOT NULL, \
    ROWNO             VARCHAR(3)    NOT NULL, \
    ITEMCD            VARCHAR(3)    NOT NULL, \
    RECORD            DECIMAL(6,3)          , \
    SCORE             SMALLINT              , \
    VALUE             VARCHAR(2)            , \
    TOTAL             VARCHAR(2)            , \
    HEIGHT            DECIMAL(4, 1)         , \
    WEIGHT            DECIMAL(4, 1)         , \
    SITHEIGHT         DECIMAL(4, 1)         , \
    EXECDATE          DATE                  , \
    WEATHER           VARCHAR(30)           , \
    TEMPERATURE       VARCHAR(5)            , \
    REGISTERCD        VARCHAR(10)           , \
    UPDATED           TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SPORTS_SCORE_ADDITION521_DAT ADD CONSTRAINT PK_SPORTS_SC_ADD521_DAT PRIMARY KEY (EDBOARD_SCHOOLCD, YEAR, GRADE, COURSECD, MAJORCD, SEX, ROWNO, ITEMCD)
