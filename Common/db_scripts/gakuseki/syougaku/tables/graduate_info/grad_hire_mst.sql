
drop table grad_hire_mst

create table grad_hire_mst \
	(hireyear	varchar(4)	not null, \
	 hirecd		varchar(4)	not null, \
	 inoutpref	varchar(1), \
	 type		varchar(10), \
	 company	varchar(20), \
	 job_m		varchar(30), \
	 job_f		varchar(30), \
	 job_type	varchar(20), \
	 zipcd		varchar(8), \
	 address	varchar(50), \
	 telno		varchar(14), \
	 faxno		varchar(14), \
	 remark		varchar(40), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table grad_hire_mst add constraint pk_grahire_mst primary key \
	(hireyear, hirecd)
