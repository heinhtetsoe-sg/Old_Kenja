
drop table attendclass_dat

create table attendclass_dat \
	(year			varchar(4)	not null, \
	 attendclasscd		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 row			varchar(2), \
	 column			varchar(2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table attendclass_dat add constraint pk_attendclass_dat primary key \
	(year, attendclasscd, schregno)


		
