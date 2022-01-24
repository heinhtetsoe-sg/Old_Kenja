
drop table planresearch_dat

create table planresearch_dat \
	(schregno		varchar(6)	not null, \
	 researchyear		varchar(4)	not null, \
	 researchdate		date		not null, \
	 plandecision		varchar(1), \
	 plancd			varchar(1), \
	 planundecided		varchar(1), \
	 plankind		varchar(2), \
	 plancd1		varchar(4), \
	 planname1		varchar(50), \
	 plancd2		varchar(4), \
	 planname2		varchar(50), \
	 regioncd		varchar(1), \
	 jobcd			varchar(2), \
	 planofficecd		varchar(2), \
	 counselorcd		varchar(2), \
	 studytime		varchar(2), \
	 tv_timecd		varchar(2), \
	 studydistraction	varchar(2), \
	 studysolution		varchar(2), \
	 planseminar		varchar(2), \
	 jobhunttestcd		varchar(2), \
	 morningstudy		varchar(2), \
	 planoccupation		varchar(2), \
	 cramcd1		varchar(2), \
	 cramcd2		varchar(2), \
	 cramcd3		varchar(2), \
	 studytime2		varchar(2), \
	 spare1			varchar(2), \
	 spare2			varchar(2), \
	 spare3			varchar(2), \
	 spare4			varchar(2), \
	 spare5			varchar(2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table planresearch_dat add constraint pk_research_dat primary key \
	(schregno, researchyear, researchdate)


