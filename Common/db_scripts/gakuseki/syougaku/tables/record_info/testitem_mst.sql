
drop table testitem_mst

create table testitem_mst \
	(year			varchar(4)	not null, \
	 subclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 testitemname		varchar(20), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table testitem_mst add constraint pk_testitem_mst primary key \
	(year,subclasscd, testkindcd, testitemcd)


