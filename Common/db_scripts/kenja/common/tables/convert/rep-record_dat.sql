-- $Id: 93f49ea6030c2d12d3a872326dfe545264b1800c $

DROP TABLE RECORD_DAT_OLD
RENAME TABLE RECORD_DAT TO RECORD_DAT_OLD
CREATE TABLE RECORD_DAT( \
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

insert into RECORD_DAT \
SELECT \
    YEAR, \
    LEFT(SUBCLASSCD, 2) AS CLASSCD, \
    'H' AS SCHOOL_KIND, \
    '2' AS CURRICULUM_CD, \
    SUBCLASSCD, \
    TAKESEMES, \
    SCHREGNO, \
    CHAIRCD, \
    SEM1_INTR_CHAIRCD,  \
    SEM1_TERM_CHAIRCD,  \
    cast(null as varchar(7)), \
    SEM2_INTR_CHAIRCD,  \
    SEM2_TERM_CHAIRCD,  \
    SEM2_TERM2_CHAIRCD,  \
    SEM3_INTR_CHAIRCD,  \
    SEM3_TERM_CHAIRCD,  \
    SEM1_INTR_SCORE, \
    SEM1_TERM_SCORE, \
    cast(null as smallint), \
    SEM2_INTR_SCORE, \
    SEM2_TERM_SCORE, \
    SEM2_TERM2_SCORE, \
    SEM3_INTR_SCORE, \
    SEM3_TERM_SCORE, \
    SEM1_INTR_VALUE, \
    SEM1_TERM_VALUE, \
    cast(null as smallint), \
    SEM1_VALUE, \
    SEM2_INTR_VALUE, \
    SEM2_TERM_VALUE, \
    SEM2_TERM2_VALUE, \
    SEM2_VALUE, \
    SEM3_INTR_VALUE, \
    SEM3_TERM_VALUE, \
    SEM3_VALUE, \
    GRAD_VALUE, \
    GRAD_VALUE2, \
    GET_CREDIT, \
    ADD_CREDIT, \
    COMP_TAKESEMES, \
    COMP_CREDIT, \
    SEM1_INTR_SCORE_DI, \
    SEM1_TERM_SCORE_DI, \
    SEM2_INTR_SCORE_DI, \
    SEM2_TERM_SCORE_DI, \
    SEM3_INTR_SCORE_DI, \
    SEM3_TERM_SCORE_DI, \
    SEM1_INTR_VALUE_DI,  \
    SEM1_TERM_VALUE_DI,  \
    cast(null as varchar(2)), \
    SEM1_VALUE_DI,  \
    SEM2_INTR_VALUE_DI,  \
    SEM2_TERM_VALUE_DI,  \
    SEM2_TERM2_VALUE_DI, \
    SEM2_VALUE_DI,  \
    SEM3_INTR_VALUE_DI,  \
    SEM3_TERM_VALUE_DI,  \
    SEM3_VALUE_DI,  \
    GRAD_VALUE_DI, \
    REGISTERCD, \
    UPDATED \
FROM \
    RECORD_DAT_OLD

alter table RECORD_DAT  \
add constraint PK_RECORD_DAT  \
primary key  \
(YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, TAKESEMES, SCHREGNO)

