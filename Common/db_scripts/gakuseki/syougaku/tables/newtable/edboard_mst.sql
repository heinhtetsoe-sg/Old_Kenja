
drop table edboard_mst

create table edboard_mst \
	(edboardcd	varchar(6) 	not null, \
	 edboardname	varchar(40), \
	 edboardabbv	varchar(10), \
	 updated	timestamp default current timestamp \
	)

alter table edboard_mst add constraint pk_edboard_mst primary key \
	(edboardcd)
