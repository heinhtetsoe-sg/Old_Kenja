
drop table classhour_mst

create table classhour_mst \
	(hourcd			varchar(2)	not null, \
	 classhour		smallint, \
	 classhoursign		varchar(2), \
	 howto			varchar(10), \
	 hoursumcd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table classhour_mst add constraint pk_clshour_mst primary key (hourcd)


