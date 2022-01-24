drop table viewname_mst

create table viewname_mst \
	 (viewcd		varchar(4)	not null, \
	 viewname		varchar(50), \
	 showorder		smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table viewname_mst add constraint pk_viewname_mst primary key (viewcd)


