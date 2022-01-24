-- $Id: 34d28216fd581a5eb5302a06922f486ae9ec94f0 $

DROP TABLE MEDEXAM_DET_NOT_EXAMINED_DAT_OLD
RENAME TABLE MEDEXAM_DET_NOT_EXAMINED_DAT TO MEDEXAM_DET_NOT_EXAMINED_DAT_OLD
CREATE TABLE MEDEXAM_DET_NOT_EXAMINED_DAT( \
    YEAR                    varchar(4)  not null, \
    GRADE                   varchar(2)  not null, \
    HEIGHT                  varchar(1), \
    WEIGHT                  varchar(1), \
    SITHEIGHT               varchar(1), \
    CHEST                   varchar(1), \
    R_BAREVISION            varchar(1), \
    R_BAREVISION_MARK       varchar(1), \
    L_BAREVISION            varchar(1), \
    L_BAREVISION_MARK       varchar(1), \
    R_VISION                varchar(1), \
    R_VISION_MARK           varchar(1), \
    L_VISION                varchar(1), \
    L_VISION_MARK           varchar(1), \
    VISION_CANTMEASURE      varchar(1), \
    R_VISION_CANTMEASURE    varchar(1), \
    L_VISION_CANTMEASURE    varchar(1), \
    R_EAR                   varchar(1), \
    R_EAR_DB                varchar(1), \
    R_EAR_DB_4000           varchar(1), \
    R_EAR_CANTMEASURE       varchar(1), \
    L_EAR                   varchar(1), \
    L_EAR_DB                varchar(1), \
    L_EAR_DB_4000           varchar(1), \
    L_EAR_CANTMEASURE       varchar(1), \
    ALBUMINURIA1CD          varchar(1), \
    URICSUGAR1CD            varchar(1), \
    URICBLEED1CD            varchar(1), \
    ALBUMINURIA2CD          varchar(1), \
    URICSUGAR2CD            varchar(1), \
    URICBLEED2CD            varchar(1), \
    URICOTHERTESTCD         varchar(1), \
    URICOTHERTEST           varchar(1), \
    URI_ADVISECD            varchar(1), \
    NUTRITIONCD             varchar(1), \
    NUTRITIONCD_REMARK      varchar(1), \
    SPINERIBCD              varchar(1), \
    SPINERIBCD_REMARK       varchar(1), \
    EYEDISEASECD            varchar(1), \
    EYEDISEASECD2           varchar(1), \
    EYEDISEASECD3           varchar(1), \
    EYEDISEASECD4           varchar(1), \
    EYEDISEASECD5           varchar(1), \
    EYE_TEST_RESULT         varchar(1), \
    EYE_TEST_RESULT2        varchar(1), \
    EYE_TEST_RESULT3        varchar(1), \
    NOSEDISEASECD           varchar(1), \
    NOSEDISEASECD2          varchar(1), \
    NOSEDISEASECD3          varchar(1), \
    NOSEDISEASECD4          varchar(1), \
    NOSEDISEASECD5          varchar(1), \
    NOSEDISEASECD6          varchar(1), \
    NOSEDISEASECD7          varchar(1), \
    NOSEDISEASECD_REMARK    varchar(1), \
    NOSEDISEASECD_REMARK1   varchar(1), \
    NOSEDISEASECD_REMARK2   varchar(1), \
    NOSEDISEASECD_REMARK3   varchar(1), \
    SKINDISEASECD           varchar(1), \
    SKINDISEASECD_REMARK    varchar(1), \
    HEART_MEDEXAM           varchar(1), \
    HEART_MEDEXAM_REMARK    varchar(1), \
    HEARTDISEASECD          varchar(1), \
    HEARTDISEASECD_REMARK   varchar(1), \
    MANAGEMENT_DIV          varchar(1), \
    MANAGEMENT_REMARK       varchar(1), \
    TB_DATE                 varchar(1), \
    TB_REACT                varchar(1), \
    TB_RESULT               varchar(1), \
    TB_BCGDATE              varchar(1), \
    TB_FILMDATE             varchar(1), \
    TB_FILMNO               varchar(1), \
    TB_REMARKCD             varchar(1), \
    TB_OTHERTESTCD          varchar(1), \
    TB_OTHERTEST_REMARK1    varchar(1), \
    TB_NAMECD               varchar(1), \
    TB_NAME_REMARK1         varchar(1), \
    TB_ADVISECD             varchar(1), \
    TB_ADVISE_REMARK1       varchar(1), \
    TB_X_RAY                varchar(1), \
    ANEMIA_REMARK           varchar(1), \
    HEMOGLOBIN              varchar(1), \
    PARASITE                varchar(1), \
    OTHERDISEASECD          varchar(1), \
    OTHER_ADVISECD          varchar(1), \
    OTHER_REMARK            varchar(1), \
    OTHER_REMARK2           varchar(1), \
    OTHER_REMARK3           varchar(1), \
    DOC_CD                  varchar(1), \
    DOC_REMARK              varchar(1), \
    DOC_DATE                varchar(1), \
    TREATCD                 varchar(1), \
    TREATCD2                varchar(1), \
    TREAT_REMARK1           varchar(1), \
    TREAT_REMARK2           varchar(1), \
    TREAT_REMARK3           varchar(1), \
    REMARK                  varchar(1), \
    NUTRITION_RESULT        varchar(1), \
    EYEDISEASE_RESULT       varchar(1), \
    SKINDISEASE_RESULT      varchar(1), \
    SPINERIB_RESULT         varchar(1), \
    NOSEDISEASE_RESULT      varchar(1), \
    OTHERDISEASE_RESULT     varchar(1), \
    HEARTDISEASE_RESULT     varchar(1), \
    GUIDE_DIV               varchar(1), \
    JOINING_SPORTS_CLUB     varchar(1), \
    MEDICAL_HISTORY1        varchar(1), \
    MEDICAL_HISTORY2        varchar(1), \
    MEDICAL_HISTORY3        varchar(1), \
    DIAGNOSIS_NAME          varchar(1), \
    MESSAGE                 varchar(1), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO MEDEXAM_DET_NOT_EXAMINED_DAT \
    SELECT \
        YEAR, \
        GRADE, \
        HEIGHT, \
        WEIGHT, \
        SITHEIGHT, \
        CHEST, \
        R_BAREVISION, \
        R_BAREVISION_MARK, \
        L_BAREVISION, \
        L_BAREVISION_MARK, \
        R_VISION, \
        R_VISION_MARK, \
        L_VISION, \
        L_VISION_MARK, \
        VISION_CANTMEASURE, \
        cast(null as varchar(1)) as R_VISION_CANTMEASURE, \
        cast(null as varchar(1)) as L_VISION_CANTMEASURE, \
        R_EAR, \
        R_EAR_DB, \
        R_EAR_DB_4000, \
        cast(null as varchar(1)) as R_EAR_CANTMEASURE, \
        L_EAR, \
        L_EAR_DB, \
        L_EAR_DB_4000, \
        cast(null as varchar(1)) as L_EAR_CANTMEASURE, \
        ALBUMINURIA1CD, \
        URICSUGAR1CD, \
        URICBLEED1CD, \
        ALBUMINURIA2CD, \
        URICSUGAR2CD, \
        URICBLEED2CD, \
        URICOTHERTESTCD, \
        URICOTHERTEST, \
        URI_ADVISECD, \
        NUTRITIONCD, \
        NUTRITIONCD_REMARK, \
        SPINERIBCD, \
        SPINERIBCD_REMARK, \
        EYEDISEASECD, \
        EYEDISEASECD2, \
        EYEDISEASECD3, \
        EYEDISEASECD4, \
        EYEDISEASECD5, \
        EYE_TEST_RESULT, \
        EYE_TEST_RESULT2, \
        EYE_TEST_RESULT3, \
        NOSEDISEASECD, \
        NOSEDISEASECD2, \
        NOSEDISEASECD3, \
        NOSEDISEASECD4, \
        NOSEDISEASECD5, \
        NOSEDISEASECD6, \
        NOSEDISEASECD7, \
        NOSEDISEASECD_REMARK, \
        NOSEDISEASECD_REMARK1, \
        NOSEDISEASECD_REMARK2, \
        NOSEDISEASECD_REMARK3, \
        SKINDISEASECD, \
        SKINDISEASECD_REMARK, \
        HEART_MEDEXAM, \
        HEART_MEDEXAM_REMARK, \
        HEARTDISEASECD, \
        HEARTDISEASECD_REMARK, \
        MANAGEMENT_DIV, \
        MANAGEMENT_REMARK, \
        TB_DATE, \
        TB_REACT, \
        TB_RESULT, \
        TB_BCGDATE, \
        TB_FILMDATE, \
        TB_FILMNO, \
        TB_REMARKCD, \
        TB_OTHERTESTCD, \
        TB_OTHERTEST_REMARK1, \
        TB_NAMECD, \
        TB_NAME_REMARK1, \
        TB_ADVISECD, \
        TB_ADVISE_REMARK1, \
        TB_X_RAY, \
        ANEMIA_REMARK, \
        HEMOGLOBIN, \
        PARASITE, \
        OTHERDISEASECD, \
        OTHER_ADVISECD, \
        OTHER_REMARK, \
        OTHER_REMARK2, \
        OTHER_REMARK3, \
        DOC_CD, \
        DOC_REMARK, \
        DOC_DATE, \
        TREATCD, \
        TREATCD2, \
        TREAT_REMARK1, \
        TREAT_REMARK2, \
        TREAT_REMARK3, \
        REMARK, \
        NUTRITION_RESULT, \
        EYEDISEASE_RESULT, \
        SKINDISEASE_RESULT, \
        SPINERIB_RESULT, \
        NOSEDISEASE_RESULT, \
        OTHERDISEASE_RESULT, \
        HEARTDISEASE_RESULT, \
        GUIDE_DIV, \
        JOINING_SPORTS_CLUB, \
        MEDICAL_HISTORY1, \
        MEDICAL_HISTORY2, \
        MEDICAL_HISTORY3, \
        DIAGNOSIS_NAME, \
        MESSAGE, \
        REGISTERCD, \
        UPDATED \
    FROM \
        MEDEXAM_DET_NOT_EXAMINED_DAT_OLD

alter table MEDEXAM_DET_NOT_EXAMINED_DAT add constraint PK_MED_DET_NO_E_D primary key (YEAR, GRADE)
