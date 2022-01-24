--近大用成績データ
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

