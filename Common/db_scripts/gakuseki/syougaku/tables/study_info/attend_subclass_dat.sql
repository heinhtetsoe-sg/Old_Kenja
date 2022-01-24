
drop table attend_subclass_dat

create table attend_subclass_dat \
	(copycd			varchar(1)	not null, \
	 year			varchar(4)	not null, \
	 semester		varchar(1)	not null, \
	 schregno		varchar(6)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 a_present		smallint, \
	 a_absent		smallint, \
	 a_suspend		smallint, \
	 a_mourning		smallint, \
	 a_sick			smallint, \
	 a_late			smallint, \
	 a_notice		smallint, \
	 a_nonotice		smallint, \
	 a_nurseoff		smallint, \
	 a_late1		smallint, \
	 a_early1		smallint, \ 
	 present		smallint, \
	 absent			smallint, \
	 suspend		smallint, \
	 mourning		smallint, \
	 sick			smallint, \
	 late			smallint, \
	 notice			smallint, \
	 nonotice		smallint, \
	 nurseoff		smallint, \
 	 late1			smallint, \
	 early1			smallint, \
	 readchangecd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table attend_subclass_dat add constraint pk_attendsub_dat primary key \
	(copycd,year,schregno, semester, classcd, subclasscd)


