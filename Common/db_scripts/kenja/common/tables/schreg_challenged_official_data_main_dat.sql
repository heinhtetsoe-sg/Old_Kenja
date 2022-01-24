-- $Id: 0f7d623f7a5f1981ab2d47f715b835420b048753 $

DROP TABLE SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT
CREATE TABLE SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT( \
    YEAR                                VARCHAR(4)    NOT NULL, \
    SCHREGNO                            VARCHAR(8)    NOT NULL, \
    RECORD_DATE                         VARCHAR(10)   NOT NULL, \
    WRITING_DATE                        DATE          NOT NULL, \
    WRITER                              VARCHAR(54), \
    MEETING_NAME                        VARCHAR(120), \
    MEETING_DATE                        DATE, \
    MEETING_SHOUR                       VARCHAR(2), \
    MEETING_SMINUTES                    VARCHAR(2), \
    MEETING_EHOUR                       VARCHAR(2), \
    MEETING_EMINUTES                    VARCHAR(2), \
    MEETING_PALCE                       VARCHAR(54), \
    MEETING_PARTICIPANT                 VARCHAR(250), \
    REGISTERCD                          VARCHAR(8), \
    UPDATED                             TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE SCHREG_CHALLENGED_OFFICIAL_DATA_MAIN_DAT ADD CONSTRAINT PK_SCH_CHA_OF_D_M PRIMARY KEY (YEAR, SCHREGNO, RECORD_DATE)