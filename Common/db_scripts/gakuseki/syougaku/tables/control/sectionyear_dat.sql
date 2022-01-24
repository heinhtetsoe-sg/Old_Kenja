
drop table sectionyear_dat

create table sectionyear_dat \
	(sectionyear		varchar(4)	not null, \
	 sectioncd		varchar(4)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table sectionyear_dat add constraint pk_secyear_dat primary key \
	(sectionyear, sectioncd)


