-- $Id: rep-kin_record_dat.sql 72507 2020-02-20 04:53:41Z yamashiro $

DROP TABLE KIN_RECORD_DAT_OLD
CREATE TABLE KIN_RECORD_DAT_OLD LIKE KIN_RECORD_DAT
INSERT INTO KIN_RECORD_DAT_OLD SELECT * FROM KIN_RECORD_DAT

DROP   TABLE KIN_RECORD_DAT
CREATE TABLE KIN_RECORD_DAT \
(  \
        "YEAR"                          VARCHAR(4)      NOT NULL, \
        "CLASSCD"                       VARCHAR(2)      NOT NULL, \
        "SCHOOL_KIND"                   VARCHAR(2)      NOT NULL, \
        "CURRICULUM_CD"                 VARCHAR(2)      NOT NULL, \
        "SUBCLASSCD"                    VARCHAR(6)      NOT NULL, \
        "SCHREGNO"                      VARCHAR(8)      NOT NULL, \
        "CHAIRCD"                       VARCHAR(7), \
        "SEM1_INTER_REC"                SMALLINT, \
        "SEM1_TERM_REC"                 SMALLINT, \
        "SEM1_REC"                      SMALLINT, \
        "SEM1_ASSESS"                   VARCHAR(1), \
        "SEM2_INTER_REC"                SMALLINT, \
        "SEM2_TERM_REC"                 SMALLINT, \
        "SEM2_REC"                      SMALLINT, \
        "SEM2_ASSESS"                   VARCHAR(1), \
        "SEM3_TERM_REC"                 SMALLINT, \
        "SEM3_REC"                      SMALLINT, \
        "SEM3_ASSESS"                   VARCHAR(1), \
        "SEM1_INTER_REC_FLG"            VARCHAR(1), \
        "SEM1_TERM_REC_FLG"             VARCHAR(1), \
        "SEM1_REC_FLG"                  VARCHAR(1), \
        "SEM2_INTER_REC_FLG"            VARCHAR(1), \
        "SEM2_TERM_REC_FLG"             VARCHAR(1), \
        "SEM2_REC_FLG"                  VARCHAR(1), \
        "SEM3_TERM_REC_FLG"             VARCHAR(1), \
        "SEM3_REC_FLG"                  VARCHAR(1), \
        "SEM1_INTER_REC_DI"             VARCHAR(2), \
        "SEM1_TERM_REC_DI"              VARCHAR(2), \
        "SEM2_INTER_REC_DI"             VARCHAR(2), \
        "SEM2_TERM_REC_DI"              VARCHAR(2), \
        "SEM3_TERM_REC_DI"              VARCHAR(2), \
        "GRADE_RECORD"                  SMALLINT, \
        "A_PATTERN_ASSESS"              VARCHAR(1), \
        "B_PATTERN_ASSESS"              VARCHAR(1), \
        "C_PATTERN_ASSESS"              VARCHAR(1), \
        "JUDGE_PATTERN"                 VARCHAR(1), \
        "GRADE_ASSESS"                  VARCHAR(1), \
        "GRADE3_RELAASSESS_5STEP"       VARCHAR(1), \
        "GRADE3_RELAASSESS_10STEP"      VARCHAR(2), \
        "REGISTERCD"                    VARCHAR(8),  \
        "UPDATED"                       TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE KIN_RECORD_DAT \
ADD CONSTRAINT PK_KIN_RECORD_DAT  \
PRIMARY KEY  \
( \
YEAR, \
CLASSCD, \
SCHOOL_KIND, \
CURRICULUM_CD, \
SUBCLASSCD, \
SCHREGNO \
)

INSERT INTO KIN_RECORD_DAT \
SELECT \
    YEAR, \
    CLASSCD, \
    SCHOOL_KIND, \
    CURRICULUM_CD, \
    SUBCLASSCD, \
    SCHREGNO, \
    CHAIRCD, \
    SEM1_INTER_REC, \
    SEM1_TERM_REC, \
    SEM1_REC, \
    cast(null as varchar(1)) as SEM1_ASSESS, \
    SEM2_INTER_REC, \
    SEM2_TERM_REC, \
    SEM2_REC, \
    cast(null as varchar(1)) as SEM2_ASSESS, \
    SEM3_TERM_REC, \
    SEM3_REC, \
    cast(null as varchar(1)) as SEM3_ASSESS, \
    SEM1_INTER_REC_FLG, \
    SEM1_TERM_REC_FLG, \
    SEM1_REC_FLG, \
    SEM2_INTER_REC_FLG, \
    SEM2_TERM_REC_FLG, \
    SEM2_REC_FLG, \
    SEM3_TERM_REC_FLG, \
    SEM3_REC_FLG, \
    SEM1_INTER_REC_DI, \
    SEM1_TERM_REC_DI, \
    SEM2_INTER_REC_DI, \
    SEM2_TERM_REC_DI, \
    SEM3_TERM_REC_DI, \
    GRADE_RECORD, \
    A_PATTERN_ASSESS, \
    B_PATTERN_ASSESS, \
    C_PATTERN_ASSESS, \
    JUDGE_PATTERN, \
    GRADE_ASSESS, \
    GRADE3_RELAASSESS_5STEP, \
    GRADE3_RELAASSESS_10STEP, \
    REGISTERCD, \
    UPDATED \
FROM KIN_RECORD_DAT_OLD
