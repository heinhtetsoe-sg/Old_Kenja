drop table viewnameyear_dat

create table viewnameyear_dat \
	(viewyear		varchar(4)	not null, \
	 viewcd			varchar(4)	not null, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table viewnameyear_dat add constraint pk_viewname_dat primary key \
	(viewyear, viewcd)


