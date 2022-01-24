-- $Id: 1b9ce4c77dad70be66b4b1e9097194fbf946586a $

DROP TABLE SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT
CREATE TABLE SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT( \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    RECORD_DATE             VARCHAR(10)   NOT NULL, \
    RECORD_SEQ              SMALLINT      NOT NULL, \
    CHECK_DATE              DATE, \
    CHECK_REMARK            VARCHAR(900), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CHECK_RECORD_DAT ADD CONSTRAINT PK_SCH_CH_TS_C_R_D PRIMARY KEY (SCHREGNO, RECORD_DATE, RECORD_SEQ)