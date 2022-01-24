-- $Id: d38b29519d1b80eaff770b6205163f6abf56b271 $

DROP TABLE ATTEND_SEMES_DEL_DAT
CREATE TABLE ATTEND_SEMES_DEL_DAT( \
    EXECUTEDATE     DATE            NOT NULL, \
    SEQ             VARCHAR(3)      NOT NULL, \
    COPYCD          VARCHAR(1)      NOT NULL, \
    YEAR            VARCHAR(4)      NOT NULL, \
    MONTH           VARCHAR(2)      NOT NULL, \
    SEMESTER        VARCHAR(1)      NOT NULL, \
    SCHREGNO        VARCHAR(8)      NOT NULL, \
    APPOINTED_DAY   VARCHAR(2), \
    LESSON          SMALLINT, \
    OFFDAYS         SMALLINT, \
    ABSENT          SMALLINT, \
    SUSPEND         SMALLINT, \
    MOURNING        SMALLINT, \
    ABROAD          SMALLINT, \
    SICK            SMALLINT, \
    NOTICE          SMALLINT, \
    NONOTICE        SMALLINT, \
    LATE            SMALLINT, \
    EARLY           SMALLINT, \
    KEKKA_JISU      SMALLINT, \
    KEKKA           SMALLINT, \
    LATEDETAIL      SMALLINT, \
    VIRUS           SMALLINT, \
    KOUDOME         SMALLINT, \
    DEL_REGISTERCD  VARCHAR(10), \
    DEL_UPDATED     TIMESTAMP, \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SEMES_DEL_DAT ADD CONSTRAINT PK_ATTSEM_DEL_DAT \
      PRIMARY KEY (EXECUTEDATE, SEQ, COPYCD, YEAR, MONTH, SEMESTER, SCHREGNO)