-- $Id: 6a9b7b33fee6d1579af96b9c673eeb849dd59310 $

drop table MEDEXAM_DISEASE_ADDITION334_FIXED_DAT

create table MEDEXAM_DISEASE_ADDITION334_FIXED_DAT( \
    EDBOARD_SCHOOLCD VARCHAR(12)    NOT NULL, \ 
    YEAR             VARCHAR(4)     NOT NULL , \ 
    FIXED_DATE       DATE           NOT NULL , \ 
    GRADE            VARCHAR(2)     NOT NULL , \ 
    SEQ              VARCHAR(2)     NOT NULL , \ 
    SCHOOL_KIND      VARCHAR(2)     NOT NULL ,  \
    GRADE_CD         VARCHAR(2)     NOT NULL ,  \
    INT_VAL          INTEGER             , \ 
    CHAR_VAL         VARCHAR(150)        , \ 
    REGISTERCD       VARCHAR(10)         , \ 
    UPDATED          timestamp default current timestamp  \ 
) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION334_FIXED_DAT add constraint PK_MED_DIS_ADD334_FIX_DAT primary key (EDBOARD_SCHOOLCD, YEAR, FIXED_DATE, GRADE, SEQ)