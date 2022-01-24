-- $Id: 2c589e04c7eb40c750dd3b0167f59dd82cb64765 $

DROP TABLE CREDIT_SPECIAL_MST
CREATE TABLE CREDIT_SPECIAL_MST( \
    YEAR                 VARCHAR(4)    NOT NULL, \
    COURSECD             VARCHAR(1)    NOT NULL, \
    MAJORCD              VARCHAR(3)    NOT NULL, \
    GRADE                VARCHAR(2)    NOT NULL, \
    COURSECODE           VARCHAR(4)    NOT NULL, \
    SPECIAL_GROUP_CD     VARCHAR(3)    NOT NULL, \
    CREDITS              SMALLINT, \
    ABSENCE_HIGH         DECIMAL(4,1), \
    GET_ABSENCE_HIGH     DECIMAL(4,1), \
    ABSENCE_WARN         SMALLINT, \
    ABSENCE_WARN2        SMALLINT, \
    ABSENCE_WARN3        SMALLINT, \
    REQUIRE_FLG          VARCHAR(1), \
    AUTHORIZE_FLG        VARCHAR(1), \
    COMP_UNCONDITION_FLG VARCHAR(1), \
    REGISTERCD           VARCHAR(8), \
    UPDATED              TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CREDIT_SPECIAL_MST ADD CONSTRAINT PK_CREDIT_SPE_MST PRIMARY KEY (YEAR,COURSECD,MAJORCD,GRADE,COURSECODE,SPECIAL_GROUP_CD)