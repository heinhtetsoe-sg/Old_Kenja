-- $Id: 8228303f58054a00049b2cfd529b7991463e4329 $

DROP TABLE DELBK_SCHREG_ENVIR_DAT
CREATE TABLE DELBK_SCHREG_ENVIR_DAT ( \
    DEL_SEQ            SMALLINT    NOT NULL, \
    SCHREGNO           VARCHAR(8)  NOT NULL, \
    GO_HOME_GROUP_NO   VARCHAR(2),   \
    RESPONSIBILITY     VARCHAR(1),   \
    HOWTOCOMMUTECD     VARCHAR(1),   \
    COMMUTE_HOURS      VARCHAR(2),   \
    COMMUTE_MINUTES    VARCHAR(2),   \
    OTHERHOWTOCOMMUTE  VARCHAR(30),  \
    STATIONNAME        VARCHAR(30),  \
    JOSYA_1            VARCHAR(45),  \
    ROSEN_1            VARCHAR(45),  \
    GESYA_1            VARCHAR(45),  \
    FLG_1              VARCHAR(1),   \
    JOSYA_2            VARCHAR(45),  \
    ROSEN_2            VARCHAR(45),  \
    GESYA_2            VARCHAR(45),  \
    FLG_2              VARCHAR(1),   \
    JOSYA_3            VARCHAR(45),  \
    ROSEN_3            VARCHAR(45),  \
    GESYA_3            VARCHAR(45),  \
    FLG_3              VARCHAR(1),   \
    JOSYA_4            VARCHAR(45),  \
    ROSEN_4            VARCHAR(45),  \
    GESYA_4            VARCHAR(45),  \
    FLG_4              VARCHAR(1),   \
    JOSYA_5            VARCHAR(45),  \
    ROSEN_5            VARCHAR(45),  \
    GESYA_5            VARCHAR(45),  \
    FLG_5              VARCHAR(1),   \
    JOSYA_6            VARCHAR(45),  \
    ROSEN_6            VARCHAR(45),  \
    GESYA_6            VARCHAR(45),  \
    FLG_6              VARCHAR(1),   \
    JOSYA_7            VARCHAR(45),  \
    ROSEN_7            VARCHAR(45),  \
    GESYA_7            VARCHAR(45),  \
    FLG_7              VARCHAR(1),   \
    BRO_SISCD          VARCHAR(1),   \
    RESIDENTCD         VARCHAR(1),   \
    DISEASE            VARCHAR(30),  \
    HEALTHCONDITION    VARCHAR(30),  \
    MERITS             VARCHAR(63),  \
    DEMERITS           VARCHAR(63),  \
    OLD_CRAM           VARCHAR(63),  \
    CUR_CRAMCD         VARCHAR(1),   \
    CUR_CRAM           VARCHAR(30),  \
    LESSONCD           VARCHAR(1),   \
    LESSON             VARCHAR(30),  \
    BEDTIME_HOURS      VARCHAR(2),   \
    BEDTIME_MINUTES    VARCHAR(2),   \
    RISINGTIME_HOURS   VARCHAR(2),   \
    RISINGTIME_MINUTES VARCHAR(2),   \
    STUDYTIME          VARCHAR(1),   \
    POCKETMONEYCD      VARCHAR(1),   \
    POCKETMONEY        SMALLINT,     \
    TVVIEWINGHOURSCD   VARCHAR(1),   \
    TVPROGRAM          VARCHAR(30),  \
    PC_HOURS           VARCHAR(1),   \
    GOOD_SUBJECT       VARCHAR(63),  \
    BAD_SUBJECT        VARCHAR(63),  \
    HOBBY              VARCHAR(63),  \
    PRIZES             VARCHAR(129), \
    READING            VARCHAR(63),  \
    SPORTS             VARCHAR(63),  \
    FRIENDSHIP         VARCHAR(63),  \
    PLANUNIV           VARCHAR(63),  \
    PLANJOB            VARCHAR(63),  \
    ED_ACT             VARCHAR(63),  \
    REMARK             VARCHAR(129), \
    REGISTERCD         VARCHAR(8),   \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    DEL_REGISTERCD     VARCHAR(8), \
    DEL_UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DELBK_SCHREG_ENVIR_DAT ADD CONSTRAINT PK_DLBK_SHG_ENVIR PRIMARY KEY (DEL_SEQ, SCHREGNO)
