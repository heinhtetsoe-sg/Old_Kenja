
drop table user_mst

create table user_mst \
	(staffcd	varchar(6)	not null, \
	 userid		varchar(60), \
	 passwd		varchar(60), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table user_mst add constraint pk_user_mst primary key (staffcd)


