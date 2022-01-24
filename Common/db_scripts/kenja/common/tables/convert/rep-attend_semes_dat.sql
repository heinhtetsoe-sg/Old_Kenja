-- $Id: 00a8b1cc61b7c42b4d3d96be2142c458bdb2b2d4 $

DROP TABLE ATTEND_SEMES_DAT_OLD
RENAME TABLE ATTEND_SEMES_DAT TO ATTEND_SEMES_DAT_OLD
CREATE TABLE ATTEND_SEMES_DAT( \
    COPYCD        VARCHAR(1)    NOT NULL, \
    YEAR          VARCHAR(4)    NOT NULL, \
    MONTH         VARCHAR(2)    NOT NULL, \
    SEMESTER      VARCHAR(1)    NOT NULL, \
    SCHREGNO      VARCHAR(8)    NOT NULL, \
    APPOINTED_DAY VARCHAR(2), \
    LESSON        SMALLINT, \
    OFFDAYS       SMALLINT, \
    ABSENT        SMALLINT, \
    SUSPEND       SMALLINT, \
    MOURNING      SMALLINT, \
    ABROAD        SMALLINT, \
    SICK          SMALLINT, \
    NOTICE        SMALLINT, \
    NONOTICE      SMALLINT, \
    LATE          SMALLINT, \
    EARLY         SMALLINT, \
    KEKKA_JISU    SMALLINT, \
    KEKKA         SMALLINT, \
    LATEDETAIL    SMALLINT, \
    VIRUS         SMALLINT, \
    KOUDOME       SMALLINT, \
    REGISTERCD    VARCHAR(10), \
    UPDATED       TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO ATTEND_SEMES_DAT \
    SELECT \
        COPYCD, \
        YEAR, \
        MONTH, \
        SEMESTER, \
        SCHREGNO, \
        APPOINTED_DAY, \
        LESSON, \
        OFFDAYS, \
        ABSENT, \
        SUSPEND, \
        MOURNING, \
        ABROAD, \
        SICK, \
        NOTICE, \
        NONOTICE, \
        LATE, \
        EARLY, \
        KEKKA_JISU, \
        KEKKA, \
        LATEDETAIL, \
        VIRUS, \
        CAST(NULL AS SMALLINT) AS KOUDOME, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ATTEND_SEMES_DAT_OLD

ALTER TABLE ATTEND_SEMES_DAT ADD CONSTRAINT PK_ATTSEMES_DAT PRIMARY KEY (COPYCD,YEAR,MONTH,SEMESTER,SCHREGNO)