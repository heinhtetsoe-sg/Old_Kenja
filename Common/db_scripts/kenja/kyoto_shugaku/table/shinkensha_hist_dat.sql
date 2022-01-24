-- $Id: shinkensha_hist_dat.sql 74435 2020-05-20 07:59:16Z yamashiro $

DROP TABLE SHINKENSHA_HIST_DAT
CREATE TABLE SHINKENSHA_HIST_DAT( \
    SHINKEN_CD               VARCHAR(7)     NOT NULL, \
    ISSUEDATE                DATE                   , \
    FAMILY_NAME              VARCHAR(60)    NOT NULL, \
    FIRST_NAME               VARCHAR(60)    NOT NULL, \
    FAMILY_NAME_KANA         VARCHAR(120)   NOT NULL, \
    FIRST_NAME_KANA          VARCHAR(120)   NOT NULL, \
    BIRTHDAY                 DATE, \
    SHINSEI_NENREI           SMALLINT, \
    TSUZUKIGARA_CD           VARCHAR(2), \
    TSUZUKIGARA_REMARK       VARCHAR(120), \
    ZIPCD                    VARCHAR(8), \
    CITYCD                   VARCHAR(5), \
    ADDR1                    VARCHAR(150), \
    ADDR2                    VARCHAR(150), \
    TELNO1                   VARCHAR(14), \
    TELNO2                   VARCHAR(14), \
    OLD_SHINKEN_NAME1        VARCHAR(60), \
    OLD_SHINKEN_NAME2        VARCHAR(60), \
    REMARK                   VARCHAR(1200), \
    REGISTERCD               VARCHAR(8), \
    UPDATED                  TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SHINKENSHA_HIST_DAT ADD CONSTRAINT PK_SHIN_H_DAT PRIMARY KEY (SHINKEN_CD)
