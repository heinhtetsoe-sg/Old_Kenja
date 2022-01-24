-- $Id: finhighschool_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

DROP TABLE FINHIGHSCHOOL_MST
CREATE TABLE FINHIGHSCHOOL_MST( \
    FINSCHOOLCD         VARCHAR(7)    NOT NULL, \
    FINSCHOOL_TYPE      VARCHAR(1), \
    FINSCHOOL_DISTCD    VARCHAR(4), \
    FINSCHOOL_DISTCD2   VARCHAR(3), \
    FINSCHOOL_DIV       VARCHAR(1), \
    FINSCHOOL_NAME      VARCHAR(75), \
    FINSCHOOL_KANA      VARCHAR(75), \
    FINSCHOOL_NAME_ABBV VARCHAR(30), \
    FINSCHOOL_KANA_ABBV VARCHAR(75), \
    PRINCNAME           VARCHAR(60), \
    PRINCNAME_SHOW      VARCHAR(30), \
    PRINCKANA           VARCHAR(120), \
    DISTRICTCD          VARCHAR(2), \
    FINSCHOOL_PREF_CD   VARCHAR(2), \
    FINSCHOOL_ZIPCD     VARCHAR(8), \
    FINSCHOOL_ADDR1     VARCHAR(75), \
    FINSCHOOL_ADDR2     VARCHAR(75), \
    FINSCHOOL_TELNO     VARCHAR(14), \
    FINSCHOOL_FAXNO     VARCHAR(14), \
    EDBOARDCD           VARCHAR(6), \
    REGISTERCD          VARCHAR(8), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE FINHIGHSCHOOL_MST ADD CONSTRAINT PK_FINHIGHSCHOOL_M PRIMARY KEY (FINSCHOOLCD)