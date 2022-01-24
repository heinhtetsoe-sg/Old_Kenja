-- $Id: 98002c587ac5ef99ec6726657c35e380067bf32a $

drop table MEDEXAM_DISEASE_ADDITION334_DAT

create table MEDEXAM_DISEASE_ADDITION334_DAT( \
    EDBOARD_SCHOOLCD VARCHAR(12) not null   ,  \
    YEAR             VARCHAR(4)  not null   ,  \
    GRADE            VARCHAR(2)  not null   ,  \
    SEQ              VARCHAR(2)  not null   ,  \
    SCHOOL_KIND      VARCHAR(2)  not null   ,  \
    GRADE_CD         VARCHAR(2)  not null   ,  \
    INT_VAL          INTEGER                ,  \
    CHAR_VAL         VARCHAR(150)           ,  \
    REGISTERCD       VARCHAR(10)            ,  \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MEDEXAM_DISEASE_ADDITION334_DAT add constraint PK_EXAM_DIS_ADD334_DAT primary key (EDBOARD_SCHOOLCD, YEAR, GRADE, SEQ)
