-- $Id: f8c95eaffaf93fa16c9a31e5fa6123acac442ad1 $

DROP TABLE SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT
CREATE TABLE SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT( \
    YEAR                                VARCHAR(4)    NOT NULL, \
    SCHREGNO                            VARCHAR(8)    NOT NULL, \
    RECORD_DATE                         VARCHAR(10)   NOT NULL, \
    DATA_DIV                            VARCHAR(2)    NOT NULL, \
    STATUS                              VARCHAR(4000), \
    GROWUP                              VARCHAR(1800), \
    REGISTERCD                          VARCHAR(10), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN usr16dms index in idx1dms

ALTER TABLE SCHREG_CHALLENGED_ASSESSMENT_STATUS_GROWUP_DAT ADD CONSTRAINT PK_SCH_CHA_AS_SG_D PRIMARY KEY (YEAR, SCHREGNO, RECORD_DATE, DATA_DIV)