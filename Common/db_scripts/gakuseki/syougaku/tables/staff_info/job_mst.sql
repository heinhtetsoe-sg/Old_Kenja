
drop table job_mst

create table job_mst \
	(jobcd		varchar(4)	not null, \
	 jobname	varchar(12), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table job_mst add constraint pk_job_mst primary key (jobcd)


