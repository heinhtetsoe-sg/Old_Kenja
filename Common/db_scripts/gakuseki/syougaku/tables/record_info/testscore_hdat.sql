
drop table testscore_hdat

create table testscore_hdat \
	(year			varchar(4)	not null, \
	 semester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 attendclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 groupcd		smallint, \
	 studyrec_date		date, \
	 perfect		smallint, \
	 rate			smallint, \
	 grades_std		varchar(1), \
	 avg_score		dec(4,1), \
	 mod_avg_score		dec(4,1), \
	 mod_flg		varchar(1), \
	 mod_std_score		smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table testscore_hdat add constraint pk_ri_hdat primary key \
	(year, semester, classcd, subclasscd, attendclasscd, testkindcd, testitemcd)
