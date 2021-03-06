-- $Id: e40729f022be8fd4b2c8227af40a63242a6a59d3 $

DROP VIEW V_APPOINTED_DAY_GRADE_MST
DROP TABLE APPOINTED_DAY_MST_OLD
RENAME TABLE APPOINTED_DAY_MST TO APPOINTED_DAY_MST_OLD
CREATE TABLE APPOINTED_DAY_MST( \
        YEAR           VARCHAR(4) NOT NULL, \
        SCHOOL_KIND    VARCHAR(2) NOT NULL, \
        MONTH          VARCHAR(2) NOT NULL, \
        SEMESTER       VARCHAR(1) NOT NULL, \
        APPOINTED_DAY  VARCHAR(2) NOT NULL, \
        REGISTERCD     VARCHAR(10), \
        UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE APPOINTED_DAY_MST ADD CONSTRAINT PK_APPOINTED_DAY_M PRIMARY KEY (YEAR, SCHOOL_KIND, MONTH, SEMESTER)

INSERT INTO APPOINTED_DAY_MST \
    SELECT \
        T1.YEAR           , \
        T2.SCHOOL_KIND    , \
        T1.MONTH          , \
        T1.SEMESTER       , \
        T1.APPOINTED_DAY  , \
        T1.REGISTERCD     , \
        T1.UPDATED          \
    FROM \
        APPOINTED_DAY_MST_OLD T1 \
        INNER JOIN (SELECT DISTINCT YEAR, SCHOOL_KIND \
                    FROM SCHREG_REGD_GDAT \
                    ) T2 ON T2.YEAR = T1.YEAR

