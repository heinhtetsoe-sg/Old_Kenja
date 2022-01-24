-- $Id$

DROP TABLE FINSCHOOL_MST
CREATE TABLE FINSCHOOL_MST( \
    FINSCHOOLCD         VARCHAR(12)    NOT NULL, \
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
    FINSCHOOL_ADDR1     VARCHAR(150), \
    FINSCHOOL_ADDR2     VARCHAR(150), \
    FINSCHOOL_TELNO     VARCHAR(14), \
    FINSCHOOL_FAXNO     VARCHAR(14), \
    EDBOARDCD           VARCHAR(6), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    FINSCHOOL_STAFFCD   VARCHAR(10), \
    FINSCHOOL_CITY_CD   VARCHAR(3) \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE FINSCHOOL_MST ADD CONSTRAINT PK_FINSCHOOL_MST PRIMARY KEY (FINSCHOOLCD)