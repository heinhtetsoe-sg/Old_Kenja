
drop table subclassyear_dat

create table subclassyear_dat \
	(subclassyear		varchar(4)	not null, \
	 subclasscd		varchar(4)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table subclassyear_dat add constraint pk_scy_dat primary key \
	(subclassyear, subclasscd)


