
drop table classyear_dat

create table classyear_dat \
	(classyear		varchar(4)	not null, \
	 classcd		varchar(2)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table classyear_dat add constraint pk_classyear_dat primary key \
	(classyear, classcd)


