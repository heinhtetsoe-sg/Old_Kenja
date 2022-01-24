DROP TABLE ATTEND_SUBCLASS_DAT_OLD

CREATE TABLE ATTEND_SUBCLASS_DAT_OLD LIKE ATTEND_SUBCLASS_DAT

INSERT INTO ATTEND_SUBCLASS_DAT_OLD SELECT * from ATTEND_SUBCLASS_DAT

DROP TABLE ATTEND_SUBCLASS_DAT

CREATE TABLE ATTEND_SUBCLASS_DAT \
    (COPYCD         varchar(1) not null, \
     YEAR			varchar(4) not null, \
     MONTH   		varchar(2) not null, \
     SEMESTER       varchar(1) not null, \
     SCHREGNO   	varchar(8) not null, \
     CLASSCD	   	varchar(2) not null, \
     SUBCLASSCD   	varchar(6) not null, \
     APPOINTED_DAY  varchar(2), \
     LESSON     	smallint, \
     OFFDAYS     	smallint, \
     ABSENT     	smallint, \
     SUSPEND     	smallint, \
     MOURNING     	smallint, \
     ABROAD     	smallint, \
     SICK     		smallint, \
     NOTICE     	smallint, \
     NONOTICE     	smallint, \
	 NURSEOFF		smallint, \
     LATE     		smallint, \
     EARLY     		smallint, \
	 VIRUS          smallint, \
     REGISTERCD     varchar(8), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE ATTEND_SUBCLASS_DAT add constraint PK_ATND_SBCLS_DAT primary key (COPYCD,YEAR,MONTH,SEMESTER,SCHREGNO,CLASSCD,SUBCLASSCD)

INSERT INTO ATTEND_SUBCLASS_DAT  SELECT \
	COPYCD, \
	YEAR, \
	MONTH, \
	SEMESTER, \
	SCHREGNO, \
	CLASSCD, \
	SUBCLASSCD, \
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
	NURSEOFF, \
	LATE, \
	EARLY, \
    cast(null as smallint), \
	REGISTERCD, \
	UPDATED \
FROM ATTEND_SUBCLASS_DAT_OLD
