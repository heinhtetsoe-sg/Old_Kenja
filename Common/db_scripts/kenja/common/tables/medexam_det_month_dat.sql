-- $Id: 862f936a8ed61ff94e397b4c0dcb5194e25e0d56 $

drop table MEDEXAM_DET_MONTH_DAT

create table MEDEXAM_DET_MONTH_DAT ( \
    YEAR                VARCHAR(4)    NOT NULL, \
    SEMESTER            VARCHAR(1)    NOT NULL, \
    MONTH               VARCHAR(2)    NOT NULL, \
    SCHREGNO            VARCHAR(8)    NOT NULL, \
    HEIGHT              DECIMAL(4,1) , \
    WEIGHT              DECIMAL(4,1) , \
    R_BAREVISION        VARCHAR(5), \
    R_BAREVISION_MARK   VARCHAR(3), \
    L_BAREVISION        VARCHAR(5), \
    L_BAREVISION_MARK   VARCHAR(3), \
    R_VISION            VARCHAR(5), \
    R_VISION_MARK       VARCHAR(3), \
    L_VISION            VARCHAR(5), \
    L_VISION_MARK       VARCHAR(3), \
    REGISTERCD          VARCHAR(10)  , \
    UPDATED             TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table MEDEXAM_DET_MONTH_DAT add constraint PK_MED_D_MNT_D primary key (YEAR,SEMESTER,MONTH,SCHREGNO)

