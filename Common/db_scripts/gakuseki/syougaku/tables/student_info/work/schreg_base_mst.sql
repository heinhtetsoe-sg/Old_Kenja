
drop table schreg_base_mst2

create table schreg_base_mst2 \
	(schregno		varchar(6)	not null, \
	 lname			varchar(20), \
	 fname			varchar(20), \
	 lkana			varchar(40), \
	 fkana			varchar(40) \
	) in usr1dms index in idx1dms

alter table schreg_base_mst2 add constraint pk_schreg_base_ms2 primary key (schregno)


