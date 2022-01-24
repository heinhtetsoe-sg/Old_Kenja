-- $Id: d881b773f7889c4a3903a0c8e00097b5a06b003b $

DROP TABLE WORK_ADVICE_CENTER_MST
CREATE TABLE WORK_ADVICE_CENTER_MST( \
    CENTERCD                VARCHAR(5)    NOT NULL, \
    NAME                    VARCHAR(150), \
    ABBV                    VARCHAR(60), \
    AREACD                  VARCHAR(2), \
    ZIPCD                   VARCHAR(8), \
    ADDR1                   VARCHAR(150), \
    ADDR2                   VARCHAR(150), \
    ADDR3                   VARCHAR(150), \
    AREA_LOCAL              VARCHAR(150), \
    TELNO                   VARCHAR(14), \
    FAXNO                   VARCHAR(14), \
    HOME_PAGE               VARCHAR(50), \
    EMAIL                   VARCHAR(50), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE WORK_ADVICE_CENTER_MST ADD CONSTRAINT PK_WO_ADVICE_C_MST PRIMARY KEY (CENTERCD)