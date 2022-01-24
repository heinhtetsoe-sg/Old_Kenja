-- $Id: 1e81f48288dfae04bb6bfabd5ba31fdaafe5a80d $

DROP TABLE SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT
CREATE TABLE SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT( \
    YEAR                                VARCHAR(4)    NOT NULL, \
    SCHREGNO                            VARCHAR(8)    NOT NULL, \
    RECORD_DATE                         VARCHAR(10)   NOT NULL, \
    DATA_DIV                            VARCHAR(2)    NOT NULL, \
    CAN_BE_NO_SUPPORT                   VARCHAR(210), \
    CAN_BE_SOME_SUPPORT                 VARCHAR(210), \
    MEANS                               VARCHAR(210), \
    SHORT_TERM_GOAL                     VARCHAR(210), \
    GOAL_FUTURE                         VARCHAR(210), \
    REGISTERCD                          VARCHAR(8), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_TEACHER_TAKEOVER_DOCUMENTS_DAT ADD CONSTRAINT PK_SCH_CHA_TC_T_DC PRIMARY KEY (YEAR, SCHREGNO, RECORD_DATE, DATA_DIV)