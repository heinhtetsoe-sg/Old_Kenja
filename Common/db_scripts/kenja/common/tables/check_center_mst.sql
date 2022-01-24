-- $Id: a4ef842e1a1ee0a2f8247adf588535f5ff59e874 $

DROP TABLE CHECK_CENTER_MST
CREATE TABLE CHECK_CENTER_MST( \
    CENTERCD                VARCHAR(5)    NOT NULL, \
    NAME                    VARCHAR(150), \
    ABBV                    VARCHAR(60), \
    AREACD                  VARCHAR(2), \
    ZIPCD                   VARCHAR(8), \
    ADDR1                   VARCHAR(150), \
    ADDR2                   VARCHAR(150), \
    ADDR3                   VARCHAR(150), \
    AREA_LOCAL              VARCHAR(60), \
    TELNO                   VARCHAR(14), \
    FAXNO                   VARCHAR(14), \
    HOME_PAGE               VARCHAR(50), \
    EMAIL                   VARCHAR(50), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE CHECK_CENTER_MST ADD CONSTRAINT PK_CHECK_CENT_MST PRIMARY KEY (CENTERCD)