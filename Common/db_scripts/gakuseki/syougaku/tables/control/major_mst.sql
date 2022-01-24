
drop table major_mst

create table major_mst \
	(coursecd		varchar(1)	not null, \
	 majorcd		varchar(3)	not null, \
	 majorname		varchar(10), \
	 majorabbv		varchar(4), \
	 majoreng		varchar(20), \
	 majorbankcd		varchar(2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table major_mst add constraint pk_major_mst primary key \
	(coursecd, majorcd)



