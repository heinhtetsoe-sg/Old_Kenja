
drop table testkind_mst

create table testkind_mst \
	(testkindcd		varchar(2)	not null, \
	 testkindname		varchar(10), \
	 testitemaddcd		varchar(1), \
	 reportoutputcd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table testkind_mst add constraint pk_testkind_mst	primary key (testkindcd)
