
drop table gradingclass_mst

create table gradingclass_mst \
	(year			varchar(4)	not null, \
	 grade			varchar(1)	not null, \
	 attendsubclasscd	varchar(4)	not null, \
	 gradingclasscd		varchar(4)	not null, \
	 gradingcd	 	varchar(1), \
	 truncatecd		varchar(1), \
	 subclasses		varchar(2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table gradingclass_mst add constraint pk_gradecls_mst primary key \
	(year, grade, attendsubclasscd, gradingclasscd)

