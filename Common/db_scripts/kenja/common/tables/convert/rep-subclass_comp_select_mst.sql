-- kanji=漢字
-- $Id: 12c44fe7e468a92beae7e401dc5bc4cb36b586cf $

DROP TABLE SUBCLASS_COMP_SELECT_MST_OLD
CREATE TABLE SUBCLASS_COMP_SELECT_MST_OLD LIKE SUBCLASS_COMP_SELECT_MST
INSERT INTO SUBCLASS_COMP_SELECT_MST_OLD SELECT * FROM SUBCLASS_COMP_SELECT_MST
DROP TABLE SUBCLASS_COMP_SELECT_MST
CREATE TABLE SUBCLASS_COMP_SELECT_MST( \
    YEAR       varchar(4)    not null, \
    GRADE      varchar(2)    not null, \
    COURSECD   varchar(1)    not null, \
    MAJORCD    varchar(3)    not null, \
    COURSECODE varchar(4)    not null, \
    GROUPCD    varchar(3)    not null, \
    NAME       varchar(60), \
    ABBV       varchar(9), \
    CREDITS    smallint, \
    JOUGEN     smallint, \
    KAGEN      smallint, \
    REGISTERCD varchar(8), \
    UPDATED    timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS


INSERT INTO SUBCLASS_COMP_SELECT_MST \
    SELECT \
        T1.YEAR, \
        T1.GRADE, \
        CM.COURSECD, \
        CM.MAJORCD, \
        CC.COURSECODE, \
        T1.GROUPCD, \
        T1.NAME, \
        T1.ABBV, \
        T1.CREDITS, \
        CAST(NULL AS SMALLINT) AS JOUGEN, \
        CAST(NULL AS SMALLINT) AS KAGEN, \
        T1.REGISTERCD, \
        T1.UPDATED \
    FROM \
        SUBCLASS_COMP_SELECT_MST_OLD T1 \
        LEFT JOIN V_COURSE_MAJOR_MST CM ON T1.YEAR = CM.YEAR \
        LEFT JOIN V_COURSECODE_MST CC ON T1.YEAR = CC.YEAR

ALTER TABLE SUBCLASS_COMP_SELECT_MST ADD CONSTRAINT PK_SUBCLASS_CSM PRIMARY KEY (YEAR, GRADE, COURSECD, MAJORCD, COURSECODE, GROUPCD)