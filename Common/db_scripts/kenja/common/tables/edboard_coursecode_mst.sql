-- $Id: b93041c7b76f2f4388aaf35c36dcf55cb96819a6 $

DROP TABLE EDBOARD_COURSECODE_MST
CREATE TABLE EDBOARD_COURSECODE_MST( \
    EDBOARD_SCHOOLCD    VARCHAR(12)     NOT NULL, \
    COURSECODE          VARCHAR(4)      NOT NULL, \
    COURSECODENAME      VARCHAR(60), \
    COURSECODEABBV1     VARCHAR(60), \
    COURSECODEABBV2     VARCHAR(60), \
    COURSECODEABBV3     VARCHAR(60), \
    REGISTERCD          VARCHAR(10), \
    UPDATED             TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE EDBOARD_COURSECODE_MST ADD CONSTRAINT \
PK_ED_COURSECODE_M PRIMARY KEY (EDBOARD_SCHOOLCD, COURSECODE)
