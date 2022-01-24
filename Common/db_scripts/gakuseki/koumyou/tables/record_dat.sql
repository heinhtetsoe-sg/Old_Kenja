drop table record_dat

create table record_dat \
	(copycd			varchar(1)	not null, \
	 year			varchar(4)	not null, \
	 semester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 gradingclasscd	varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 attendclasscd	varchar(4), \
	 groupcd		smallint, \
	 test1			smallint, \
	 test2			smallint, \
	 test3			smallint, \
	 test4			smallint, \
	 educateval		smallint, \
	 tempgrades		smallint, \
	 old_tempgrades	smallint, \
	 mod_score		smallint, \
	 minusscore		smallint, \
	 grades			smallint, \
	 credits		smallint, \
	 addcreditcd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table record_dat add constraint pk_ret_dat primary key \
	(copycd,year, semester, classcd, gradingclasscd, schregno)
