
drop table dutyshare_mst

create table dutyshare_mst \
	(dutysharecd 		varchar(4)	not null, \
	 sharename		varchar(16), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table dutyshare_mst add constraint pk_dutyshare_mst primary key \
	(dutysharecd)


