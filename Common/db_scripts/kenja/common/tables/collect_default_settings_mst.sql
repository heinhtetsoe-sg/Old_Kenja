-- $Id: ed2714bed0d0d1b00e164985a50fd04c3185bd52 $

DROP TABLE COLLECT_DEFAULT_SETTINGS_MST

CREATE TABLE COLLECT_DEFAULT_SETTINGS_MST \
(  \
        "SCHOOLCD"              VARCHAR(12)     NOT NULL, \
        "SCHOOL_KIND"           VARCHAR(2)      NOT NULL, \
        "YEAR"                  VARCHAR(4)      NOT NULL, \
        "COLLECT_PATTERN_CD"    VARCHAR(2)      NOT NULL, \
        "COLLECT_PATTERN_NAME"  VARCHAR(90), \
        "DIRECT_DEBIT_DATE"     VARCHAR(2) , \
        "COLLECT_MONTH_4"       VARCHAR(2) , \
        "COLLECT_MONTH_5"       VARCHAR(2) , \
        "COLLECT_MONTH_6"       VARCHAR(2) , \
        "COLLECT_MONTH_7"       VARCHAR(2) , \
        "COLLECT_MONTH_8"       VARCHAR(2) , \
        "COLLECT_MONTH_9"       VARCHAR(2) , \
        "COLLECT_MONTH_10"      VARCHAR(2) , \
        "COLLECT_MONTH_11"      VARCHAR(2) , \
        "COLLECT_MONTH_12"      VARCHAR(2) , \
        "COLLECT_MONTH_1"       VARCHAR(2) , \
        "COLLECT_MONTH_2"       VARCHAR(2) , \
        "COLLECT_MONTH_3"       VARCHAR(2) , \
        "PAY_DIV"               VARCHAR(1) , \
        "REGISTERCD"            VARCHAR(10), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE COLLECT_DEFAULT_SETTINGS_MST  \
ADD CONSTRAINT PK_COLLECT_DEFAULT  \
PRIMARY KEY  \
(SCHOOLCD, SCHOOL_KIND, YEAR, COLLECT_PATTERN_CD)
