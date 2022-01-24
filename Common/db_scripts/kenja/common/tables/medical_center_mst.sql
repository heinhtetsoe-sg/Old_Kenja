-- $Id: ee7f6f75ab682b1883ef5c47ef1cde89dec4a050 $

DROP TABLE MEDICAL_CENTER_MST
CREATE TABLE MEDICAL_CENTER_MST( \
    CENTERCD                VARCHAR(5)    NOT NULL, \
    NAME                    VARCHAR(90), \
    ABBV                    VARCHAR(30), \
    ZIPCD                   VARCHAR(8), \
    ADDR1                   VARCHAR(150), \
    ADDR2                   VARCHAR(150), \
    TELNO                   VARCHAR(14), \
    FAXNO                   VARCHAR(14), \
    MEDICAL_DIRECTOR_NAME   VARCHAR(90), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MEDICAL_CENTER_MST ADD CONSTRAINT PK_MED_CENTER_MST PRIMARY KEY (CENTERCD)