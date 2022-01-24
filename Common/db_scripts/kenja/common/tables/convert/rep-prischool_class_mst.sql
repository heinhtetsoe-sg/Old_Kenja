-- $Id: 464c682302feea2b2afd29ca7001c95c4afcf6a2 $

DROP TABLE PRISCHOOL_CLASS_MST_OLD

RENAME TABLE PRISCHOOL_CLASS_MST TO PRISCHOOL_CLASS_MST_OLD

CREATE TABLE PRISCHOOL_CLASS_MST( \
    PRISCHOOLCD             varchar(7)   not null, \
    PRISCHOOL_CLASS_CD      varchar(7)   not null, \
    PRISCHOOL_NAME          varchar(75),  \
    PRISCHOOL_KANA          varchar(75),  \
    PRINCNAME               varchar(60),  \
    PRINCNAME_SHOW          varchar(30),  \
    PRINCKANA               varchar(120), \
    DISTRICTCD              varchar(2),   \
    PRISCHOOL_ZIPCD         varchar(8),   \
    PRISCHOOL_ADDR1         varchar(150), \
    PRISCHOOL_ADDR2         varchar(150), \
    PRISCHOOL_TELNO         varchar(14),  \
    PRISCHOOL_FAXNO         varchar(14),  \
    ROSEN_1                 varchar(45),  \
    ROSEN_2                 varchar(45),  \
    ROSEN_3                 varchar(45),  \
    ROSEN_4                 varchar(45),  \
    ROSEN_5                 varchar(45),  \
    NEAREST_STATION_NAME1   varchar(75),  \
    NEAREST_STATION_KANA1   varchar(75),  \
    NEAREST_STATION_NAME2   varchar(75),  \
    NEAREST_STATION_KANA2   varchar(75),  \
    DIRECT_MAIL_FLG         varchar(1),   \
    REGISTERCD              varchar(10),  \
    UPDATED                 timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO PRISCHOOL_CLASS_MST \
    SELECT \
        PRISCHOOLCD, \
        PRISCHOOL_CLASS_CD, \
        PRISCHOOL_NAME, \
        PRISCHOOL_KANA, \
        PRINCNAME, \
        PRINCNAME_SHOW, \
        PRINCKANA, \
        DISTRICTCD, \
        PRISCHOOL_ZIPCD, \
        PRISCHOOL_ADDR1, \
        PRISCHOOL_ADDR2, \
        PRISCHOOL_TELNO, \
        PRISCHOOL_FAXNO, \
        CAST(null AS varchar(45)) AS ROSEN_1, \
        CAST(null AS varchar(45)) AS ROSEN_2, \
        CAST(null AS varchar(45)) AS ROSEN_3, \
        CAST(null AS varchar(45)) AS ROSEN_4, \
        CAST(null AS varchar(45)) AS ROSEN_5, \
        CAST(null AS varchar(75)) AS NEAREST_STATION_NAME1, \
        CAST(null AS varchar(75)) AS NEAREST_STATION_KANA1, \
        CAST(null AS varchar(75)) AS NEAREST_STATION_NAME2, \
        CAST(null AS varchar(75)) AS NEAREST_STATION_KANA2, \
        CAST(null AS varchar(1))  AS DIRECT_MAIL_FLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        PRISCHOOL_CLASS_MST_OLD

ALTER TABLE PRISCHOOL_CLASS_MST ADD CONSTRAINT PK_PRISCH_CLASS_M PRIMARY KEY (PRISCHOOLCD, PRISCHOOL_CLASS_CD)