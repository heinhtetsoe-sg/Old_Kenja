-- $Id: 07ad62c5ee53d20d60cf328f707568894bba56e2 $

drop table MEDEXAM_CARE_HDAT_OLD

rename table MEDEXAM_CARE_HDAT to MEDEXAM_CARE_HDAT_OLD

create table MEDEXAM_CARE_HDAT( \
    YEAR             VARCHAR(4)     NOT NULL, \
    SCHREGNO         VARCHAR(8)     NOT NULL, \
    CARE_DIV         VARCHAR(2)     NOT NULL, \
    CARE_FLG         VARCHAR(1)    , \
    EMERGENCYNAME    VARCHAR(30)   , \
    EMERGENCYTELNO   VARCHAR(14)   , \
    EMERGENCYNAME2   VARCHAR(120)  , \
    EMERGENCYTELNO2  VARCHAR(14)   , \
    DATE             DATE          , \
    DOCTOR           VARCHAR(30)   , \
    HOSPITAL         VARCHAR(120)  , \
    REMARK           VARCHAR(2100) , \
    REGISTERCD       VARCHAR(10)   , \
    UPDATED          TIMESTAMP      DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table MEDEXAM_CARE_HDAT add constraint PK_MEDEXAM_C_H primary key (YEAR,SCHREGNO,CARE_DIV)

insert into MEDEXAM_CARE_HDAT( \
    YEAR, \
    SCHREGNO, \
    CARE_DIV, \
    CARE_FLG, \
    EMERGENCYNAME, \
    EMERGENCYTELNO, \
    EMERGENCYNAME2, \
    EMERGENCYTELNO2, \
    DATE, \
    DOCTOR, \
    HOSPITAL, \
    REMARK, \
    REGISTERCD, \
    UPDATED \
 ) select  \
    YEAR, \
    SCHREGNO, \
    CARE_DIV, \
    CARE_FLG, \
    EMERGENCYNAME, \
    EMERGENCYTELNO, \
    EMERGENCYNAME2, \
    EMERGENCYTELNO2, \
    DATE, \
    DOCTOR, \
    HOSPITAL, \
    REMARK, \
    REGISTERCD, \
    UPDATED \
 from MEDEXAM_CARE_HDAT_OLD

