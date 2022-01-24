-- $Id: record_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table RECORD_DAT
create table RECORD_DAT  \
(  \
        YEAR                  varchar(4)      not null, \
        CLASSCD               varchar(2)      not null, \
        CURRICULUM_CD         varchar(1)      not null, \
        SUBCLASSCD            varchar(6)      not null, \
        TAKESEMES             varchar(1)      not null, \
        SCHREGNO              varchar(8)      not null, \
        CHAIRCD               varchar(7),  \
        SEM1_INTR_CHAIRCD     varchar(7),  \
        SEM1_TERM_CHAIRCD     varchar(7),  \
        SEM1_TERM2_CHAIRCD    varchar(7),  \
        SEM2_INTR_CHAIRCD     varchar(7),  \
        SEM2_TERM_CHAIRCD     varchar(7),  \
        SEM2_TERM2_CHAIRCD    varchar(7),  \
        SEM3_INTR_CHAIRCD     varchar(7),  \
        SEM3_TERM_CHAIRCD     varchar(7),  \
        SEM1_INTR_SCORE       smallint,  \
        SEM1_TERM_SCORE       smallint,  \
        SEM1_TERM2_SCORE      smallint,  \
        SEM2_INTR_SCORE       smallint,  \
        SEM2_TERM_SCORE       smallint,  \
        SEM2_TERM2_SCORE      smallint,  \
        SEM3_INTR_SCORE       smallint,  \
        SEM3_TERM_SCORE       smallint,  \
        SEM1_INTR_VALUE       smallint,  \
        SEM1_TERM_VALUE       smallint,  \
        SEM1_TERM2_VALUE      smallint,  \
        SEM1_VALUE            smallint,  \
        SEM2_INTR_VALUE       smallint,  \
        SEM2_TERM_VALUE       smallint,  \
        SEM2_TERM2_VALUE      smallint,  \
        SEM2_VALUE            smallint,  \
        SEM3_INTR_VALUE       smallint,  \
        SEM3_TERM_VALUE       smallint,  \
        SEM3_VALUE            smallint,  \
        GRAD_VALUE            smallint,  \
        GRAD_VALUE2           smallint,  \
        GET_CREDIT            smallint,  \
        ADD_CREDIT            smallint,  \
        COMP_TAKESEMES        varchar(1),  \
        COMP_CREDIT           smallint,  \
        SEM1_INTR_SCORE_DI    varchar(2),  \
        SEM1_TERM_SCORE_DI    varchar(2),  \
        SEM2_INTR_SCORE_DI    varchar(2),  \
        SEM2_TERM_SCORE_DI    varchar(2),  \
        SEM3_INTR_SCORE_DI    varchar(2),  \
        SEM3_TERM_SCORE_DI    varchar(2),  \
        SEM1_INTR_VALUE_DI    varchar(2),  \
        SEM1_TERM_VALUE_DI    varchar(2),  \
        SEM1_TERM2_VALUE_DI   varchar(2),  \
        SEM1_VALUE_DI         varchar(2),  \
        SEM2_INTR_VALUE_DI    varchar(2),  \
        SEM2_TERM_VALUE_DI    varchar(2),  \
        SEM2_TERM2_VALUE_DI   varchar(2),  \
        SEM2_VALUE_DI         varchar(2),  \
        SEM3_INTR_VALUE_DI    varchar(2),  \
        SEM3_TERM_VALUE_DI    varchar(2),  \
        SEM3_VALUE_DI         varchar(2),  \
        GRAD_VALUE_DI         varchar(2),  \
        REGISTERCD            varchar(8),  \
        UPDATED               timestamp default current timestamp  \
) in usr1dms index in idx1dms


alter table RECORD_DAT  \
add constraint PK_RECORD_DAT  \
primary key  \
( \
YEAR, \
CLASSCD, \
CURRICULUM_CD, \
SUBCLASSCD, \
TAKESEMES, \
SCHREGNO \
)

