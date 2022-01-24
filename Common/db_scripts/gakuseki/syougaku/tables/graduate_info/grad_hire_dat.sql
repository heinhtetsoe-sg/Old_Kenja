
drop table grad_hire_dat

create table grad_hire_dat \
	(schregno	varchar(6)	not null, \
	 hireyear	varchar(4), \
	 hirecd		varchar(4), \
	 univ_cd	varchar(1), \
	 remark		varchar(50), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table grad_hire_dat add constraint pk_hire_dat primary key (schregno) 
