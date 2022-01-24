-- $Id: 261dad60fa1d8b1cb4ebe2cc0d0571eccb15f12d $

drop table SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT_OLD

rename table SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT to SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT_OLD

create table SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT( \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    RECORD_DATE             VARCHAR(10)   NOT NULL, \
    RECORD_DIV              VARCHAR(1)    NOT NULL, \
    RECORD_SEQ              SMALLINT      NOT NULL, \
    NAMECD                  VARCHAR(3), \
    CENTERCD                VARCHAR(5), \
    CENTER_NAME             VARCHAR(90), \
    TELNO                   VARCHAR(14), \
    DISEASE_NAME            VARCHAR(48), \
    DOCTOR_NAME             VARCHAR(30), \
    ATTEND_STATUS           VARCHAR(120), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

alter table SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT ADD CONSTRAINT PK_SCH_CHA_P_M_R_D PRIMARY KEY (SCHREGNO, RECORD_DATE, RECORD_DIV, RECORD_SEQ)

insert into SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT( \
    SCHREGNO, \
    RECORD_DATE, \
    RECORD_DIV, \
    RECORD_SEQ, \
    NAMECD, \
    CENTERCD, \
    CENTER_NAME, \
    TELNO, \
    DISEASE_NAME, \
    DOCTOR_NAME, \
    ATTEND_STATUS, \
    REGISTERCD, \
    UPDATED \
 ) select  \
    SCHREGNO, \
    RECORD_DATE, \
    RECORD_DIV, \
    RECORD_SEQ, \
    NAMECD, \
    CENTERCD, \
    CAST(NULL AS VARCHAR(90)), \
    TELNO, \
    DISEASE_NAME, \
    DOCTOR_NAME, \
    ATTEND_STATUS, \
    REGISTERCD, \
    UPDATED \
 from SCHREG_CHALLENGED_PROFILE_MEDICAL_RECORD_DAT_OLD

