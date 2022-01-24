-- $Id: 93751e2ef4ca96f1e4c459a2ae596a1b1bb1efa9 $
DROP TABLE EDBOARD_STAFF_IDOU_DAT_OLD
CREATE TABLE EDBOARD_STAFF_IDOU_DAT_OLD LIKE EDBOARD_STAFF_IDOU_DAT
INSERT INTO EDBOARD_STAFF_IDOU_DAT_OLD SELECT * FROM EDBOARD_STAFF_IDOU_DAT

drop table EDBOARD_STAFF_IDOU_DAT

CREATE TABLE EDBOARD_STAFF_IDOU_DAT( \
    YEAR                  VARCHAR(4)  NOT NULL, \
    STAFFCD               VARCHAR(10) NOT NULL, \
    IDOU_DIV              VARCHAR(1)  NOT NULL, \
    IDOU_DATE             DATE        NOT NULL, \
    ASSIGNMENT_DATE       DATE, \
    FROM_FINSCHOOLCD      VARCHAR(12) NOT NULL, \
    TO_FINSCHOOLCD        VARCHAR(12), \
    FROM_EDBOARD_SCHOOLCD VARCHAR(12) NOT NULL, \
    TO_EDBOARD_SCHOOLCD   VARCHAR(12), \
    REGISTERCD            VARCHAR(10), \
    UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO EDBOARD_STAFF_IDOU_DAT \
    SELECT \
        * \
    FROM \
        EDBOARD_STAFF_IDOU_DAT_OLD

ALTER TABLE EDBOARD_STAFF_IDOU_DAT ADD CONSTRAINT PK_STAFF_IDOU PRIMARY KEY (YEAR, STAFFCD)
