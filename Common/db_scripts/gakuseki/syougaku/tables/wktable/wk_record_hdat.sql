
drop table wk_record_hdat

create table wk_record_hdat \
	(copycd			varchar(1)	not null, \
	 year			varchar(4)	not null, \
	 semester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 grandingclasscd	varchar(4)	not null, \
	 attendclasscd		varchar(4)	not null, \
	 groupcd		smallint, \
	 tempassess_flg		varchar(1), \
	 mod_flg		varchar(1), \
	 avg_standard		varchar(1), \	
 	 mod_avg		smallint, \
	 minusscore_flg		varchar(1), \
	 minusscore_rate	smallint, \
	 minusscore_odd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table wk_record_hdat	add constraint pk_wkrecord_hdat primary key \
	(copycd,year, semester, classcd, subclasscd, grandingclasscd,attendclasscd)
