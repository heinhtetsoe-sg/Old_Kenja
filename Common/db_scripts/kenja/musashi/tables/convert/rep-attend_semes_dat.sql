-- $Id: rep-attend_semes_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $
DROP TABLE ATTEND_SEMES_DAT_OLD

CREATE TABLE ATTEND_SEMES_DAT_OLD LIKE ATTEND_SEMES_DAT

INSERT INTO ATTEND_SEMES_DAT_OLD SELECT * from ATTEND_SEMES_DAT

DROP TABLE ATTEND_SEMES_DAT

CREATE TABLE ATTEND_SEMES_DAT \
    (COPYCD         varchar(1) not null, \
     YEAR           varchar(4) not null, \
     MONTH          varchar(2) not null, \
     SEMESTER       varchar(1) not null, \
     SCHREGNO       varchar(8) not null, \
     APPOINTED_DAY  varchar(2), \
     LESSON         smallint, \
     OFFDAYS        smallint, \
     ABSENT         smallint, \
     SUSPEND        smallint, \
     MOURNING       smallint, \
     ABROAD         smallint, \
     SICK           smallint, \
     NOTICE         smallint, \
     NONOTICE       smallint, \
     LATE           smallint, \
     EARLY          smallint, \
     KEKKA_JISU     smallint, \
     KEKKA          smallint, \
     LATEDETAIL     smallint, \
     VIRUS          smallint, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE ATTEND_SEMES_DAT add constraint pk_at_sem_dat primary key (COPYCD,YEAR,SEMESTER,MONTH,SCHREGNO)

INSERT INTO ATTEND_SEMES_DAT  SELECT \
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
    cast(null as smallint), \
    KEKKA, \
    LATEDETAIL, \
    VIRUS, \
    REGISTERCD, \
    UPDATED \
FROM ATTEND_SEMES_DAT_OLD
