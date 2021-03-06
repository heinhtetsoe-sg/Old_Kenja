-- $Id: aab45da800323f61e610e64c6b1038aa5242d9e0 $

DROP TABLE SPORTS_SCORE_INQUIRY_ADDITION521_DAT
CREATE TABLE SPORTS_SCORE_INQUIRY_ADDITION521_DAT ( \
    EDBOARD_SCHOOLCD      VARCHAR(12)   NOT NULL, \
    YEAR                  VARCHAR(4)    NOT NULL, \
    GRADE                 VARCHAR(2)    NOT NULL, \
    COURSECD              VARCHAR(1)    NOT NULL, \
    MAJORCD               VARCHAR(3)    NOT NULL, \
    SEX                   VARCHAR(1)    NOT NULL, \
    ROWNO                 VARCHAR(3)    NOT NULL, \
    INQUIRYCD             VARCHAR(2)    NOT NULL, \
    VALUE                 VARCHAR(1)            , \
    REGISTERCD            VARCHAR(10)           , \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SPORTS_SCORE_INQUIRY_ADDITION521_DAT ADD CONSTRAINT PK_SPORTS_SC_INC_ADD_DAT PRIMARY KEY (EDBOARD_SCHOOLCD, YEAR, GRADE, COURSECD, MAJORCD, SEX, ROWNO, INQUIRYCD)
