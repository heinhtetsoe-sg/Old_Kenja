
drop table attendclass_hdat

create table attendclass_hdat \
	(year			varchar(4)	not null, \
	 attendclasscd		varchar(4)	not null, \
	 classalias		varchar(20), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table attendclass_hdat add constraint pk_ac_hdat primary key \
	(year, attendclasscd)


