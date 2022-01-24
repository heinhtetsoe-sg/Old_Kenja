-- $Id: 8927fe6e67059679207b3687823c8db44a424f9f $

DROP TABLE SCHREG_CHALLENGED_SUPPORTPLAN_WORK_RECORD_DAT
CREATE TABLE SCHREG_CHALLENGED_SUPPORTPLAN_WORK_RECORD_DAT( \
    YEAR                    VARCHAR(4)    NOT NULL, \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    RECORD_DATE             VARCHAR(10)   NOT NULL, \
    RECORD_DIV              VARCHAR(1)    NOT NULL, \
    RECORD_SEQ              SMALLINT      NOT NULL, \
    CENTERCD                VARCHAR(5), \
    S_YEAR_MONTH            VARCHAR(7), \
    STAFF_NAME              VARCHAR(120), \
    WORK_REMARK             VARCHAR(420), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_SUPPORTPLAN_WORK_RECORD_DAT ADD CONSTRAINT PK_SCH_CH_S_WO_R_D PRIMARY KEY (YEAR, SCHREGNO, RECORD_DATE, RECORD_DIV, RECORD_SEQ)