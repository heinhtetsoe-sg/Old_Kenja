
drop table electclass_mst

create table electclass_mst \
	(groupcd		smallint	not null, \
	 groupname		varchar(6), \
	 groupabbv		varchar(4), \
	 remark			varchar(60), \
         showorder              smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table electclass_mst add constraint pk_esc_mst primary key \
	(groupcd)


