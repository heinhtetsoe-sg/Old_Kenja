-- $Id: b10afed36dd4f1e5cdb2a238fecf04e4520e2722 $

DROP TABLE SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT
CREATE TABLE SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT( \
    SCHREGNO                 VARCHAR(8)    NOT NULL, \
    RECORD_DATE              VARCHAR(10)   NOT NULL, \
    RECORD_DIV               VARCHAR(1)    NOT NULL, \
    RECORD_NO                VARCHAR(1)    NOT NULL, \
    RECORD_SEQ               SMALLINT      NOT NULL, \
    SERVICE_NAME             VARCHAR(105) , \
    ITEMCD                   VARCHAR(3)   , \
    ITEMCD2                  VARCHAR(3)   , \
    ITEMCD3                  VARCHAR(3)   , \
    SUPPLY_DATE              DATE         , \
    CENTERCD                 VARCHAR(5)   , \
    SERVICE_CENTERCD         VARCHAR(10)  , \
    SERVICE_CENTERCD_EDABAN  VARCHAR(2)   , \
    SERVICE_CHARGE           VARCHAR(15)   , \
    WELFARE_REMARK           VARCHAR(120) , \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_PROFILE_WELFARE_RECORD_DAT ADD CONSTRAINT PK_SCH_CH_P_WE_R_D PRIMARY KEY (SCHREGNO, RECORD_DATE, RECORD_DIV, RECORD_NO, RECORD_SEQ)