-- $Id: 8d11e7cc9c9e5f0d79eed5be9956b6bb5cc85b9c $

DROP TABLE SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT
CREATE TABLE SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT( \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    RECORD_DATE             VARCHAR(10)   NOT NULL, \
    RECORD_SEQ              SMALLINT      NOT NULL, \
    CENTERCD                VARCHAR(5), \
    WORK_REMARK             VARCHAR(120), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_PROFILE_WORK_RECORD_DAT ADD CONSTRAINT PK_SCH_CH_P_WO_R_D PRIMARY KEY (SCHREGNO, RECORD_DATE, RECORD_SEQ)