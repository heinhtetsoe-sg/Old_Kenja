
drop table usergroup_mst

create table usergroup_mst \
	(groupcode	varchar(4)	not null, \
	 groupname	varchar(40), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table usergroup_mst add constraint pk_usrgroup_mst \
	primary key (groupcode)
