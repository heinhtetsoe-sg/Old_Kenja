-- $Id: c5644d739ecd5a90659677e457bdcfc06aee71cc $

drop table SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT
create table SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT( \
    SCHREGNO                            VARCHAR(8)    NOT NULL, \
    RECORD_DATE                         VARCHAR(10)   NOT NULL, \
    WRITING_DATE                        DATE          NOT NULL, \
    CHALLENGED_NAMES                    VARCHAR(150), \
    CHALLENGED_STATUS                   VARCHAR(390), \
    ONES_HOPE_PRESENT                   VARCHAR(150), \
    ONES_HOPE_CAREER                    VARCHAR(150), \
    ONES_HOPE_AFTER_GRADUATION          VARCHAR(150), \
    GUARDIAN_HOPE_PRESENT               VARCHAR(180), \
    GUARDIAN_HOPE_CAREER                VARCHAR(180), \
    GUARDIAN_HOPE_AFTER_GRADUATION      VARCHAR(180), \
    MEDICAL_GOAL_AFTER_GRADUATION       VARCHAR(120), \
    MEDICAL_STATUS                      VARCHAR(120), \
    MEDICAL_SHORT_TERM_GOAL             VARCHAR(120), \
    MEDICAL_TANGIBLE_SUPPORT            VARCHAR(120), \
    WELFARE_GOAL_AFTER_GRADUATION       VARCHAR(120), \
    WELFARE_STATUS                      VARCHAR(120), \
    WELFARE_SHORT_TERM_GOAL             VARCHAR(120), \
    WELFARE_TANGIBLE_SUPPORT            VARCHAR(120), \
    WORK_GOAL_AFTER_GRADUATION          VARCHAR(120), \
    WORK_STATUS                         VARCHAR(120), \
    WORK_SHORT_TERM_GOAL                VARCHAR(120), \
    WORK_TANGIBLE_SUPPORT               VARCHAR(120), \
    COMMU_GOAL_AFTER_GRADUATION         VARCHAR(120), \
    COMMU_STATUS                        VARCHAR(120), \
    COMMU_SHORT_TERM_GOAL               VARCHAR(120), \
    COMMU_TANGIBLE_SUPPORT              VARCHAR(120), \
    TAKEOVER                            VARCHAR(720), \
    REGISTERCD                          VARCHAR(10), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    RECORD_STAFFNAME                    VARCHAR(120) \
) in usr1dms index in idx1dms

alter table SCHREG_CHALLENGED_TRANSITION_SUPPORTPLAN_MAIN_DAT add constraint PK_SCH_CHA_TSP_M_D primary key (SCHREGNO, RECORD_DATE)