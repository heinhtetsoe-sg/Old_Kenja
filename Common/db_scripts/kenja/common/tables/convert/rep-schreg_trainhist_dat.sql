-- $Id: 8f683e57385cd9126a4ab731a2603fe38cc4aed4 $

DROP TABLE SCHREG_TRAINHIST_DAT_OLD
RENAME TABLE SCHREG_TRAINHIST_DAT TO SCHREG_TRAINHIST_DAT_OLD
CREATE TABLE SCHREG_TRAINHIST_DAT( \
    YEAR         VARCHAR(4)    NOT NULL, \
    TRAINDATE    DATE          NOT NULL, \
    SCHREGNO     VARCHAR(8)    NOT NULL, \
    PATIENTCD    VARCHAR(2), \
    STAFFCD      VARCHAR(8), \
    HOWTOTRAINCD VARCHAR(2), \
    CONTENT      VARCHAR(458), \
    REGISTERCD   VARCHAR(8), \
    UPDATED      TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO SCHREG_TRAINHIST_DAT \
    SELECT \
        YEAR, \
        TRAINDATE, \
        SCHREGNO, \
        PATIENTCD, \
        STAFFCD, \
        HOWTOTRAINCD, \
        CONTENT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SCHREG_TRAINHIST_DAT_OLD

ALTER TABLE SCHREG_TRAINHIST_DAT ADD CONSTRAINT PK_SCHREGTRAINHIST PRIMARY KEY (YEAR,TRAINDATE,SCHREGNO)