 
drop table electclassstaff_dat

create table electclassstaff_dat \
	(year			varchar(4)	not null, \
	 semester		varchar(1)	not null, \
	 groupcd		smallint	not null, \
	 group_seq		smallint	not null, \
	 staffcd		varchar(6)	not null, \
	 subclasscd		varchar(4), \
	 attendclasscd		varchar(4), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table electclassstaff_dat add constraint pk_ecstaff_dat primary key \
	(year, semester,groupcd,group_seq, staffcd)


