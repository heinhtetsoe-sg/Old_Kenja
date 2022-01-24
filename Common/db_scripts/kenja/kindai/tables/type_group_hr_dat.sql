--類型グループHRクラスデータ
--2004-10-27 *_SUM SMALLINT→INTEGER
DROP TABLE TYPE_GROUP_HR_DAT

CREATE TABLE TYPE_GROUP_HR_DAT  \
(  \
        "YEAR"                  VARCHAR(4)      NOT NULL, \
        "TYPE_GROUP_CD"         VARCHAR(6)      NOT NULL, \
        "GRADE"                 VARCHAR(2)      NOT NULL, \
        "HR_CLASS"              VARCHAR(3)      NOT NULL, \
        "SEM1_INTER_REC_SUM"    INTEGER, \
        "SEM1_TERM_REC_SUM"     INTEGER, \
        "SEM1_REC_SUM"          INTEGER, \
        "SEM2_INTER_REC_SUM"    INTEGER, \
        "SEM2_TERM_REC_SUM"     INTEGER, \
        "SEM2_REC_SUM"          INTEGER, \
        "SEM3_TERM_REC_SUM"     INTEGER, \
        "GRADE_RECORD_SUM"      INTEGER, \
        "SEM1_INTER_REC_CNT"    SMALLINT, \
        "SEM1_TERM_REC_CNT"     SMALLINT, \
        "SEM1_REC_CNT"          SMALLINT, \
        "SEM2_INTER_REC_CNT"    SMALLINT, \
        "SEM2_TERM_REC_CNT"     SMALLINT, \
        "SEM2_REC_CNT"          SMALLINT, \
        "SEM3_TERM_REC_CNT"     SMALLINT, \
        "GRADE_RECORD_CNT"      SMALLINT, \
        "REGISTERCD"            VARCHAR(8), \
        "UPDATED"               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE TYPE_GROUP_HR_DAT  \
ADD CONSTRAINT PK_TYPE_GRP_HR_DAT  \
PRIMARY KEY  \
(YEAR,TYPE_GROUP_CD,GRADE,HR_CLASS)
