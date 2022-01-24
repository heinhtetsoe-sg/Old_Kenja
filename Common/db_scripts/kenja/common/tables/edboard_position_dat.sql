-- $Id: d8ba5b225eb64b24f8bb7927d8f982ad30133d2c $

DROP TABLE EDBOARD_POSITION_DAT
CREATE TABLE EDBOARD_POSITION_DAT( \
    EDBOARD_SCHOOLCD  VARCHAR(12)   NOT NULL, \
    POSITIONCD        VARCHAR(4)    NOT NULL, \
    EDBOARD_FLG       VARCHAR(1), \
    REGISTERCD        VARCHAR(8), \
    UPDATED    TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EDBOARD_POSITION_DAT ADD CONSTRAINT PK_ED_POSITION_MST PRIMARY KEY (EDBOARD_SCHOOLCD, POSITIONCD)