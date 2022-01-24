
drop table usergroup_dat

create table usergroup_dat \
	(groupcode	varchar(4)	not null, \
	 staffcd	varchar(6)	not null, \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table usergroup_dat add constraint pk_usrgroup_dat \
	primary key (groupcode, staffcd)
