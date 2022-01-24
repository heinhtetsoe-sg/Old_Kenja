
drop table wk_record_dat

create table wk_record_dat \
	(copycd			varchar(1)	not null, \
	 year			varchar(4)	not null, \
	 semester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 gradingclasscd		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 attendclasscd		varchar(4), \
	 groupcd		smallint, \
	 tempgrades		smallint, \
	 old_tempgrades		smallint, \
	 mod_score		smallint, \
	 minusscore		smallint, \
	 grades			smallint, \
	 credits		smallint, \
	 addcreditcd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table wk_record_dat add constraint pk_wkret_dat primary key \
	(copycd,year, semester, classcd, subclasscd, gradingclasscd, schregno)


