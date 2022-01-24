-- $Id: c7c25b7427b8bd33a1eb0f78a552983362df4947 $

drop table RECORD_DAT
create table RECORD_DAT  \
(  \
        YEAR                  VARCHAR(4)      NOT NULL, \
        CLASSCD               VARCHAR(2)      NOT NULL, \
        SCHOOL_KIND           VARCHAR(2)      NOT NULL, \
        CURRICULUM_CD         VARCHAR(2)      NOT NULL, \
        SUBCLASSCD            VARCHAR(6)      NOT NULL, \
        TAKESEMES             VARCHAR(1)      NOT NULL, \
        SCHREGNO              VARCHAR(8)      NOT NULL, \
        CHAIRCD               VARCHAR(7),  \
        SEM1_INTR_CHAIRCD     VARCHAR(7),  \
        SEM1_TERM_CHAIRCD     VARCHAR(7),  \
        SEM1_TERM2_CHAIRCD    VARCHAR(7),  \
        SEM2_INTR_CHAIRCD     VARCHAR(7),  \
        SEM2_TERM_CHAIRCD     VARCHAR(7),  \
        SEM2_TERM2_CHAIRCD    VARCHAR(7),  \
        SEM3_INTR_CHAIRCD     VARCHAR(7),  \
        SEM3_TERM_CHAIRCD     VARCHAR(7),  \
        SEM1_INTR_SCORE       SMALLINT,  \
        SEM1_TERM_SCORE       SMALLINT,  \
        SEM1_TERM2_SCORE      SMALLINT,  \
        SEM2_INTR_SCORE       SMALLINT,  \
        SEM2_TERM_SCORE       SMALLINT,  \
        SEM2_TERM2_SCORE      SMALLINT,  \
        SEM3_INTR_SCORE       SMALLINT,  \
        SEM3_TERM_SCORE       SMALLINT,  \
        SEM1_INTR_VALUE       SMALLINT,  \
        SEM1_TERM_VALUE       SMALLINT,  \
        SEM1_TERM2_VALUE      SMALLINT,  \
        SEM1_VALUE            SMALLINT,  \
        SEM2_INTR_VALUE       SMALLINT,  \
        SEM2_TERM_VALUE       SMALLINT,  \
        SEM2_TERM2_VALUE      SMALLINT,  \
        SEM2_VALUE            SMALLINT,  \
        SEM3_INTR_VALUE       SMALLINT,  \
        SEM3_TERM_VALUE       SMALLINT,  \
        SEM3_VALUE            SMALLINT,  \
        GRAD_VALUE            SMALLINT,  \
        GRAD_VALUE2           SMALLINT,  \
        GET_CREDIT            SMALLINT,  \
        ADD_CREDIT            SMALLINT,  \
        COMP_TAKESEMES        VARCHAR(1),  \
        COMP_CREDIT           SMALLINT,  \
        SEM1_INTR_SCORE_DI    VARCHAR(2),  \
        SEM1_TERM_SCORE_DI    VARCHAR(2),  \
        SEM2_INTR_SCORE_DI    VARCHAR(2),  \
        SEM2_TERM_SCORE_DI    VARCHAR(2),  \
        SEM3_INTR_SCORE_DI    VARCHAR(2),  \
        SEM3_TERM_SCORE_DI    VARCHAR(2),  \
        SEM1_INTR_VALUE_DI    VARCHAR(2),  \
        SEM1_TERM_VALUE_DI    VARCHAR(2),  \
        SEM1_TERM2_VALUE_DI   VARCHAR(2),  \
        SEM1_VALUE_DI         VARCHAR(2),  \
        SEM2_INTR_VALUE_DI    VARCHAR(2),  \
        SEM2_TERM_VALUE_DI    VARCHAR(2),  \
        SEM2_TERM2_VALUE_DI   VARCHAR(2),  \
        SEM2_VALUE_DI         VARCHAR(2),  \
        SEM3_INTR_VALUE_DI    VARCHAR(2),  \
        SEM3_TERM_VALUE_DI    VARCHAR(2),  \
        SEM3_VALUE_DI         VARCHAR(2),  \
        GRAD_VALUE_DI         VARCHAR(2),  \
        REGISTERCD            VARCHAR(8),  \
        UPDATED               TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms


alter table RECORD_DAT  \
add constraint PK_RECORD_DAT  \
primary key  \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, TAKESEMES, SCHREGNO)
