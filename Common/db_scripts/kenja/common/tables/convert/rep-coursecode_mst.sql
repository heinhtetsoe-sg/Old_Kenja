-- $Id: e85fbcbb69920d3d9172eb547c24f5b43d65888e $

DROP TABLE COURSECODE_MST_OLD
RENAME TABLE COURSECODE_MST TO COURSECODE_MST_OLD
CREATE TABLE COURSECODE_MST( \
    COURSECODE     VARCHAR(4)    NOT NULL, \
    COURSECODENAME VARCHAR(60), \
    COURSECODEABBV1 VARCHAR(60), \
    COURSECODEABBV2 VARCHAR(60), \
    COURSECODEABBV3 VARCHAR(60), \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO COURSECODE_MST \
    SELECT \
        COURSECODE, \
        COURSECODENAME, \
        CAST(NULL AS VARCHAR(60)), \
        CAST(NULL AS VARCHAR(60)), \
        CAST(NULL AS VARCHAR(60)), \
        REGISTERCD, \
        UPDATED \
    FROM \
        COURSECODE_MST_OLD

ALTER TABLE COURSECODE_MST ADD CONSTRAINT PK_COURSECODE_MST PRIMARY KEY (COURSECODE)