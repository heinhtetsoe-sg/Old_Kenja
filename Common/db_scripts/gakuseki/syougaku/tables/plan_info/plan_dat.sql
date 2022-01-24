
drop table plan_dat

create table plan_dat \
	(planno			varchar(4)	not null, \
	 receiptyear		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 plandecisioncd		varchar(1), \
	 planrecomcd		varchar(1), \
	 inoutprefcd		varchar(1), \
	 univ_yearcd		varchar(1), \
	 univ_estabcd		varchar(1), \
	 univ_domesticcd	varchar(1), \
	 plan_namecd		varchar(25), \
	 univ_cd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table plan_dat add constraint pk_plan_dat primary key \
	(planno, receiptyear, schregno)


