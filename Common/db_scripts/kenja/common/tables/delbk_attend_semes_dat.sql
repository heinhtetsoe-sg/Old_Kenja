-- $Id: c03c9abc4f8f8343dff38575d6f1b80bfc4756d6 $

DROP TABLE DELBK_ATTEND_SEMES_DAT
CREATE TABLE DELBK_ATTEND_SEMES_DAT ( \
    DEL_SEQ            SMALLINT        NOT NULL, \
    COPYCD             VARCHAR(1)      NOT NULL, \
    YEAR               VARCHAR(4)      NOT NULL, \
    MONTH              VARCHAR(2)      NOT NULL, \
    SEMESTER           VARCHAR(1)      NOT NULL, \
    SCHREGNO           VARCHAR(8)      NOT NULL, \
    APPOINTED_DAY      VARCHAR(2), \
    LESSON             SMALLINT, \
    OFFDAYS            SMALLINT, \
    ABSENT             SMALLINT, \
    SUSPEND            SMALLINT, \
    MOURNING           SMALLINT, \
    ABROAD             SMALLINT, \
    SICK               SMALLINT, \
    NOTICE             SMALLINT, \
    NONOTICE           SMALLINT, \
    LATE               SMALLINT, \
    EARLY              SMALLINT, \
    KEKKA_JISU         SMALLINT, \
    KEKKA              SMALLINT, \
    LATEDETAIL         SMALLINT, \
    VIRUS              SMALLINT, \
    KOUDOME            SMALLINT, \
    REGISTERCD         VARCHAR(8), \
    UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
    DEL_REGISTERCD     VARCHAR(8), \
    DEL_UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE DELBK_ATTEND_SEMES_DAT ADD CONSTRAINT PK_DLBK_AT_SEMES PRIMARY KEY \
        (DEL_SEQ, COPYCD, YEAR, SEMESTER,MONTH, SCHREGNO)


