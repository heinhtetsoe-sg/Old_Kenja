
drop table courseyear_dat

create table courseyear_dat \
	(courseyear		varchar(4)	not null, \
	 coursecd		varchar(1)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table courseyear_dat add constraint pk_couyear_dat primary key \
	(courseyear, coursecd)


