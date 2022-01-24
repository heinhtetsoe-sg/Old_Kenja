
drop table process_mst

create table process_mst \
	(processcd	varchar(2)	not null, \
	 process	varchar(10), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table process_mst add constraint pk_process_mst primary key (processcd)


