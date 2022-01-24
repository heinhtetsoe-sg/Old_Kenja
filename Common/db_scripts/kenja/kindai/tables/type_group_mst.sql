--類型グループマスタ
--2004-10-27 *_SUM SMALLINT→INTEGER
--11/08追加（スポーツクラス等の固定評価用）
DROP TABLE TYPE_GROUP_MST

CREATE TABLE TYPE_GROUP_MST  \
(  \
        "YEAR"                                  VARCHAR(4)      NOT NULL, \
        "TYPE_GROUP_CD"                         VARCHAR(6)      NOT NULL, \
        "GRADE"                                 VARCHAR(2), \
        "SUBCLASSCD"                            VARCHAR(6), \
        "TYPE_GROUP_NAME"                       VARCHAR(60), \
        "TYPE_ASSES_CD"                         VARCHAR(1), \
        "SEM1_INTER_REC_SUM"                    INTEGER, \
        "SEM1_TERM_REC_SUM"                     INTEGER, \
        "SEM1_REC_SUM"                          INTEGER, \
        "SEM2_INTER_REC_SUM"                    INTEGER, \
        "SEM2_TERM_REC_SUM"                     INTEGER, \
        "SEM2_REC_SUM"                          INTEGER, \
        "SEM3_TERM_REC_SUM"                     INTEGER, \
        "GRADE_RECORD_SUM"                      INTEGER, \
        "SEM1_INTER_REC_CNT"                    SMALLINT, \
        "SEM1_TERM_REC_CNT"                     SMALLINT, \
        "SEM1_REC_CNT"                          SMALLINT, \
        "SEM2_INTER_REC_CNT"                    SMALLINT, \
        "SEM2_TERM_REC_CNT"                     SMALLINT, \
        "SEM2_REC_CNT"                          SMALLINT, \
        "SEM3_TERM_REC_CNT"                     SMALLINT, \
        "GRADE_RECORD_CNT"                      SMALLINT, \
        "SEM1_INTER_REC_TYPE_ASSES_CD"          VARCHAR(1), \
        "SEM1_TERM_REC_TYPE_ASSES_CD"           VARCHAR(1), \
        "SEM1_REC_TYPE_ASSES_CD"                VARCHAR(1), \
        "SEM2_INTER_REC_TYPE_ASSES_CD"          VARCHAR(1), \
        "SEM2_TERM_REC_TYPE_ASSES_CD"           VARCHAR(1), \
        "SEM2_REC_TYPE_ASSES_CD"                VARCHAR(1), \
        "SEM3_TERM_REC_TYPE_ASSES_CD"           VARCHAR(1), \
        "GRADE_RECORD_TYPE_ASSES_CD"            VARCHAR(1), \
        "SEM1_INTER_REC_DATE"                   TIMESTAMP, \
        "SEM1_TERM_REC_DATE"                    TIMESTAMP, \
        "SEM1_REC_DATE"                         TIMESTAMP, \
        "SEM2_INTER_REC_DATE"                   TIMESTAMP, \
        "SEM2_TERM_REC_DATE"                    TIMESTAMP, \
        "SEM2_REC_DATE"                         TIMESTAMP, \
        "SEM3_TERM_REC_DATE"                    TIMESTAMP, \
        "GRADE_RECORD_DATE"                     TIMESTAMP, \
        "REGISTERCD"                            VARCHAR(8), \
        "UPDATED"                               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE TYPE_GROUP_MST  \
ADD CONSTRAINT PK_TYPE_GROUP_MST  \
PRIMARY KEY  \
(YEAR,TYPE_GROUP_CD)

