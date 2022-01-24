-- $Id: medexam_det_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table MEDEXAM_DET_DAT

create table MEDEXAM_DET_DAT \
   (YEAR                    VARCHAR (4) not null, \
    SCHREGNO                VARCHAR (8) not null, \
    DATE                    DATE, \
    HEIGHT                  decimal (4,1), \
    WEIGHT                  decimal (4,1), \
    SITHEIGHT               decimal (4,1), \
    R_BAREVISION            VARCHAR (4), \
    R_BAREVISION_MARK       VARCHAR (3), \
    L_BAREVISION            VARCHAR (4), \
    L_BAREVISION_MARK       VARCHAR (3), \
    R_VISION                VARCHAR (4), \
    R_VISION_MARK           VARCHAR (3), \
    L_VISION                VARCHAR (4), \
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
    NUTRITIONCD             VARCHAR (2), \
    SPINERIBCD              VARCHAR (2), \
    EYEDISEASECD            VARCHAR (2), \
    NOSEDISEASECD           VARCHAR (2), \
    SKINDISEASECD           VARCHAR (2), \
    HEART_MEDEXAM           VARCHAR (2), \
    HEART_MEDEXAM_REMARK    VARCHAR (120), \
    HEARTDISEASECD          VARCHAR (2), \
    TB_DATE                 DATE, \
    TB_REACT                decimal (3,1), \
    TB_RESULT               VARCHAR (2), \
    TB_BCGDATE              DATE, \
    TB_FILMDATE             DATE, \
    TB_FILMNO               VARCHAR (6), \
    TB_REMARKCD             VARCHAR (2), \
    TB_OTHERTESTCD          VARCHAR (2), \
    TB_NAMECD               VARCHAR (2), \
    TB_ADVISECD             VARCHAR (2), \
    ANEMIA_REMARK           VARCHAR (30), \
    HEMOGLOBIN              decimal (3,1), \
    OTHERDISEASECD          VARCHAR (2), \
    DOC_REMARK              VARCHAR (30), \
    DOC_DATE                DATE, \
    TREATCD                 VARCHAR (2), \
    REMARK                  VARCHAR (30), \
    NUTRITION_RESULT        VARCHAR (60), \
    EYEDISEASE_RESULT       VARCHAR (60), \
    SKINDISEASE_RESULT      VARCHAR (60), \
    SPINERIB_RESULT         VARCHAR (60), \
    NOSEDISEASE_RESULT      VARCHAR (60), \
    OTHERDISEASE_RESULT     VARCHAR (60), \
    HEARTDISEASE_RESULT     VARCHAR (60), \
    REGISTERCD              VARCHAR (8), \
    UPDATED                timestamp default current timestamp \
   ) in usr1dms index in idx1dms

alter table MEDEXAM_DET_DAT add constraint pk_grdmdx_det_dat primary key (YEAR,SCHREGNO)


