
drop table testscore_dat

create table testscore_dat \
	(year			varchar(4)	not null, \
	 semester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 schregno		varchar(6)	not null, \
	 attendclasscd		varchar(4), \
	 groupcd		smallint, \
	 attend_flg		varchar(1), \
	 score			smallint, \
	 mod_score		smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table testscore_dat add constraint pk_ris_dat primary key \
	(year, semester, classcd, subclasscd, testkindcd, testitemcd, schregno)


