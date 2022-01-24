-- $Id: bcdfd7e51a8f80ea294bc7a527fd05880bc23746 $

DROP TABLE SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT
CREATE TABLE SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT( \
    SCHREGNO                VARCHAR(8)    NOT NULL, \
    RECORD_DATE             VARCHAR(10)   NOT NULL, \
    RECORD_DIV              VARCHAR(1)    NOT NULL, \
    RECORD_NO               VARCHAR(1)    NOT NULL, \
    RECORD_SEQ              SMALLINT      NOT NULL, \
    MEETING_NAME            VARCHAR(120), \
    MEETING_DATE            DATE, \
    TEAM_MEMBERS            VARCHAR(150), \
    MEETING_SUMMARY         VARCHAR(600), \
    WORK_TRAINING_PLACE     VARCHAR(60), \
    WORK_TRAINING_S_DATE    DATE, \
    WORK_TRAINING_E_DATE    DATE, \
    WORK_TRAINING_CONTENTS  VARCHAR(120), \
    WORK_TRAINING_GOAL      VARCHAR(600), \
    WORK_TRAINING_SUPPORT   VARCHAR(600), \
    WORK_TRAINING_SUPPORT_TARGET   VARCHAR(120), \
    WORK_TRAINING_RESULT    VARCHAR(600), \
    WORK_TRAINING_CHALLENGE VARCHAR(600), \
    CAREER_GUIDANCE_RESULT    VARCHAR(600), \
    CAREER_GUIDANCE_CHALLENGE VARCHAR(600), \
    DETERMINED_COURSE       VARCHAR(120), \
    COURSE_CONTENTS         VARCHAR(450), \
    REMARK                  VARCHAR(630), \
    REGISTERCD              VARCHAR(10), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_CAREERGUIDANCE_RECORD_DAT ADD CONSTRAINT PK_SCH_CH_TS_CG_R PRIMARY KEY (SCHREGNO, RECORD_DATE, RECORD_DIV, RECORD_NO, RECORD_SEQ)