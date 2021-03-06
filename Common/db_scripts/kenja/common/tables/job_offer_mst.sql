-- $Id: 57d523aad5c79aca35ae755a8fa4b7f6c9062573 $

DROP TABLE JOB_OFFER_MST
CREATE TABLE JOB_OFFER_MST( \
    YEAR            VARCHAR(4)    NOT NULL, \
    SENKOU_NO       INT           NOT NULL, \
    COMPANY_CD      CHAR(8), \
    COMPANY_NAME     VARCHAR(75), \
    COMPANY_NAMEKANA VARCHAR(150), \
    COMPANY_CONTENTS VARCHAR(150), \
    SIHONKIN        VARCHAR(17), \
    MAKECOMPANY_YEAR VARCHAR(4), \
    COMPANY_ZIPCD   VARCHAR(8), \
    COMPANY_ADDR1   VARCHAR(150), \
    COMPANY_ADDR2   VARCHAR(75), \
    COMPANY_TELNO1  VARCHAR(14), \
    COMPANY_TELNO2  VARCHAR(14), \
    COMPANY_FAXNO   VARCHAR(14), \
    INDUSTRY_SCD    VARCHAR(3), \
    SONINZU         INT, \
    SHUSHOKU_NAME   VARCHAR(150), \
    SHUSHOKU_ZIPCD  VARCHAR(8), \
    SHUSHOKU_ADDR1  VARCHAR(150), \
    SHUSHOKU_ADDR2  VARCHAR(75), \
    SHUSHOKU_TELNO1 VARCHAR(14), \
    SHUSHOKU_TELNO2 VARCHAR(14), \
    SHUSHOKU_FAXNO  VARCHAR(14), \
    TONINZU         INT, \
    COMPANY_SORT    CHAR(2), \
    TARGET_NINZU    SMALLINT, \
    TARGET_SEX      CHAR(1), \
    JUDGING_MEANS1  VARCHAR(2), \
    JUDGING_MEANS2  VARCHAR(2), \
    JUDGING_MEANS3  VARCHAR(2), \
    JUDGING_MEANS4  VARCHAR(2), \
    HOLIDAY1        VARCHAR(1), \
    HOLIDAY2        VARCHAR(1), \
    HOLIDAY3        VARCHAR(1), \
    HOLIDAY4        VARCHAR(1), \
    HOLIDAY_REMARK  VARCHAR(50), \
    SHIFT           VARCHAR(1), \
    COMPANY_LODGING VARCHAR(1), \
    BASIC_SALARY    INT, \
    TAKE_SALARY     INT, \
    REGISTERCD      VARCHAR(8), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE JOB_OFFER_MST ADD CONSTRAINT PK_JOB_OFFER_MST PRIMARY KEY (YEAR, SENKOU_NO)