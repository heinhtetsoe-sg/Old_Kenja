-- $Id: 445edfc123478bceacf2243cdd566e24565485b4 $

DROP TABLE WELFARE_USE_SERVICE_CENTER_MST
CREATE TABLE WELFARE_USE_SERVICE_CENTER_MST( \
    SERVICE_CENTERCD        VARCHAR(10)   NOT NULL, \
    SERVICE_CENTERCD_EDABAN VARCHAR(2)    NOT NULL, \
    NAME                    VARCHAR(150), \
    ABBV                    VARCHAR(60), \
    AREACD                  VARCHAR(2), \
    ZIPCD                   VARCHAR(8), \
    ADDR1                   VARCHAR(150), \
    ADDR2                   VARCHAR(150), \
    ADDR3                   VARCHAR(150), \
    COMMISSION_NAME         VARCHAR(90), \
    TELNO                   VARCHAR(14), \
    FAXNO                   VARCHAR(14), \
    CHALLENGED_SUPPORT_FLG  VARCHAR(1), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE WELFARE_USE_SERVICE_CENTER_MST ADD CONSTRAINT PK_WEL_U_S_CEN_MST PRIMARY KEY (SERVICE_CENTERCD, SERVICE_CENTERCD_EDABAN)