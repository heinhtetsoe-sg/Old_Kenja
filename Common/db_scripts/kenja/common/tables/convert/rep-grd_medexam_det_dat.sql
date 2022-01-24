-- $Id: d0442dc70eb5d9ad6b88960db7d831e93e830ed7 $

DROP TABLE GRD_MEDEXAM_DET_DAT_OLD
RENAME TABLE GRD_MEDEXAM_DET_DAT TO GRD_MEDEXAM_DET_DAT_OLD
CREATE TABLE GRD_MEDEXAM_DET_DAT( \
    YEAR                    VARCHAR (4) NOT NULL, \
    SCHREGNO                VARCHAR (8) NOT NULL, \
    HEIGHT                  DECIMAL (4,1), \
    WEIGHT                  DECIMAL (4,1), \
    SITHEIGHT               DECIMAL (4,1), \
    CHEST                   DECIMAL(4,1), \
    R_BAREVISION            VARCHAR (5), \
    R_BAREVISION_MARK       VARCHAR (3), \
    L_BAREVISION            VARCHAR (5), \
    L_BAREVISION_MARK       VARCHAR (3), \
    R_VISION                VARCHAR (5), \
    R_VISION_MARK           VARCHAR (3), \
    L_VISION                VARCHAR (5), \
    L_VISION_MARK           VARCHAR (3), \
    R_EAR                   VARCHAR (2), \
    R_EAR_DB                SMALLINT, \
    L_EAR                   VARCHAR (2), \
    L_EAR_DB                SMALLINT, \
    ALBUMINURIA1CD          VARCHAR (2), \
    URICSUGAR1CD            VARCHAR (2), \
    URICBLEED1CD            VARCHAR (2), \
    ALBUMINURIA2CD          VARCHAR (2), \
    URICSUGAR2CD            VARCHAR (2), \
    URICBLEED2CD            VARCHAR (2), \
    URICOTHERTEST           VARCHAR (60), \
    URI_ADVISECD            VARCHAR (2), \
    NUTRITIONCD             VARCHAR (2), \
    SPINERIBCD              VARCHAR (2), \
    SPINERIBCD_REMARK       VARCHAR(60), \
    EYEDISEASECD            VARCHAR (2), \
    EYE_TEST_RESULT         VARCHAR(60), \
    NOSEDISEASECD           VARCHAR (2), \
    NOSEDISEASECD_REMARK    VARCHAR(60), \
    SKINDISEASECD           VARCHAR (2), \
    HEART_MEDEXAM           VARCHAR (2), \
    HEART_MEDEXAM_REMARK    VARCHAR (120), \
    HEARTDISEASECD          VARCHAR (2), \
    HEARTDISEASECD_REMARK   VARCHAR(60), \
    MANAGEMENT_DIV          VARCHAR(2), \
    MANAGEMENT_REMARK       VARCHAR(60), \
    TB_DATE                 DATE, \
    TB_REACT                DECIMAL (3,1), \
    TB_RESULT               VARCHAR (2), \
    TB_BCGDATE              DATE, \
    TB_FILMDATE             DATE, \
    TB_FILMNO               VARCHAR (20), \
    TB_REMARKCD             VARCHAR (2), \
    TB_OTHERTESTCD          VARCHAR (2), \
    TB_NAMECD               VARCHAR (2), \
    TB_ADVISECD             VARCHAR (2), \
    TB_X_RAY                VARCHAR(60), \
    ANEMIA_REMARK           VARCHAR (30), \
    HEMOGLOBIN              DECIMAL (3,1), \
    PARASITE                VARCHAR(2), \
    OTHERDISEASECD          VARCHAR (2), \
    OTHER_ADVISECD          VARCHAR(2), \
    OTHER_REMARK            VARCHAR(60), \
    DOC_CD                  VARCHAR(2), \
    DOC_REMARK              VARCHAR (90), \
    DOC_DATE                DATE, \
    TREATCD                 VARCHAR (2), \
    REMARK                  VARCHAR (300), \
    NUTRITION_RESULT        VARCHAR (60), \
    EYEDISEASE_RESULT       VARCHAR (60), \
    SKINDISEASE_RESULT      VARCHAR (60), \
    SPINERIB_RESULT         VARCHAR (60), \
    NOSEDISEASE_RESULT      VARCHAR (60), \
    OTHERDISEASE_RESULT     VARCHAR (60), \
    HEARTDISEASE_RESULT     VARCHAR (60), \
    GUIDE_DIV               VARCHAR(1), \
    JOINING_SPORTS_CLUB     VARCHAR(1), \
    MEDICAL_HISTORY1        VARCHAR(3), \
    MEDICAL_HISTORY2        VARCHAR(3), \
    MEDICAL_HISTORY3        VARCHAR(3), \
    DIAGNOSIS_NAME          VARCHAR(150), \
    REGISTERCD              VARCHAR (10), \
    UPDATED                timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE GRD_MEDEXAM_DET_DAT ADD CONSTRAINT PK_GRDMDX_DET_DAT PRIMARY KEY (YEAR,SCHREGNO)

INSERT INTO GRD_MEDEXAM_DET_DAT \
    SELECT \
        YEAR, \
        SCHREGNO, \
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
        R_EAR, \
        R_EAR_DB, \
        L_EAR, \
        L_EAR_DB, \
        ALBUMINURIA1CD, \
        URICSUGAR1CD, \
        URICBLEED1CD, \
        ALBUMINURIA2CD, \
        URICSUGAR2CD, \
        URICBLEED2CD, \
        URICOTHERTEST, \
        URI_ADVISECD, \
        NUTRITIONCD, \
        SPINERIBCD, \
        SPINERIBCD_REMARK, \
        EYEDISEASECD, \
        EYE_TEST_RESULT, \
        NOSEDISEASECD, \
        NOSEDISEASECD_REMARK, \
        SKINDISEASECD, \
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
        TB_NAMECD, \
        TB_ADVISECD, \
        TB_X_RAY, \
        ANEMIA_REMARK, \
        HEMOGLOBIN, \
        PARASITE, \
        OTHERDISEASECD, \
        OTHER_ADVISECD, \
        OTHER_REMARK, \
        DOC_CD, \
        DOC_REMARK, \
        DOC_DATE, \
        TREATCD, \
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
        REGISTERCD, \
        UPDATED \
    FROM \
        GRD_MEDEXAM_DET_DAT_OLD

