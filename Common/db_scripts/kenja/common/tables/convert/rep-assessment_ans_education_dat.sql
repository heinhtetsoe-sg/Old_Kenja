-- $Id: d5b9e14be45ebcd6377addb91d1aed34fae5a46a $

drop table ASSESSMENT_ANS_EDUCATION_DAT_OLD

rename table ASSESSMENT_ANS_EDUCATION_DAT to ASSESSMENT_ANS_EDUCATION_DAT_OLD

create table ASSESSMENT_ANS_EDUCATION_DAT( \
    YEAR                 VARCHAR(4)    NOT NULL, \
    SCHREGNO             VARCHAR(8)    NOT NULL, \
    P_S_YM               VARCHAR(7)   , \
    P_E_YM               VARCHAR(7)   , \
    P_PASSING_GRADE_FLG  VARCHAR(1)   , \
    P_SUPPORT_FLG        VARCHAR(1)   , \
    P_ETC_FLG            VARCHAR(1)   , \
    P_ETC                VARCHAR(18)  , \ 
    P_DATE_S_YM          VARCHAR(7)   ,\
    P_DATE_E_YM          VARCHAR(7)   ,\
    J_S_YM               VARCHAR(7)   ,\
    J_E_YM               VARCHAR(7)   ,\
    J_PASSING_GRADE_FLG  VARCHAR(1)   ,\
    J_SUPPORT_FLG        VARCHAR(1)   ,\
    J_ETC_FLG            VARCHAR(1)   ,\
    J_ETC                VARCHAR(18)   ,\
    J_DATE_S_YM          VARCHAR(7)   ,\
    J_DATE_E_YM          VARCHAR(7)   ,\
    EDUCATION_TEXT       VARCHAR(750) ,\
    REGISTERCD           VARCHAR(10)  ,\
    UPDATED              TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table ASSESSMENT_ANS_EDUCATION_DAT add constraint PK_ASSESS_ANS_EDU primary key (YEAR,SCHREGNO)

insert into ASSESSMENT_ANS_EDUCATION_DAT( \
    YEAR,                  \
    SCHREGNO,              \
    P_S_YM,                \
    P_E_YM,                \
    P_PASSING_GRADE_FLG,   \
    P_SUPPORT_FLG,         \
    P_ETC_FLG,             \
    P_ETC,                 \
    P_DATE_S_YM,           \
    P_DATE_E_YM,           \
    J_S_YM,                \
    J_E_YM,                \
    J_PASSING_GRADE_FLG,   \
    J_SUPPORT_FLG,         \
    J_ETC_FLG,             \
    J_ETC,                 \
    J_DATE_S_YM,           \
    J_DATE_E_YM,           \
    EDUCATION_TEXT,        \
    REGISTERCD,            \
    UPDATED                \
 ) select                  \
        YEAR,              \
    SCHREGNO,              \
    P_S_YM,                \
    P_E_YM,                \
    P_PASSING_GRADE_FLG,   \
    P_SUPPORT_FLG,         \
    cast(null as varchar(1)),  \
    cast(null as varchar(18)), \
    P_DATE_S_YM,           \
    P_DATE_E_YM,           \
    J_S_YM,                \
    J_E_YM,                \
    J_PASSING_GRADE_FLG,   \
    J_SUPPORT_FLG,         \
    cast(null as varchar(1)),  \
    cast(null as varchar(18)), \
    J_DATE_S_YM,           \
    J_DATE_E_YM,           \
    EDUCATION_TEXT,        \
    REGISTERCD,            \
    UPDATED                \
 from ASSESSMENT_ANS_EDUCATION_DAT_OLD
