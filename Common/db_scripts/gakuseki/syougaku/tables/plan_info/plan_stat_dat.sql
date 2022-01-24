
drop table plan_stat_dat

create table plan_stat_dat \
	(receiptyear		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 graduatedyear		varchar(4), \
	 assess_avg		dec(2,1), \
	 level			varchar(1), \
	 testrank		smallint, \
	 trialrank		smallint, \
	 testavg_rank		smallint, \
	 trialavg_rank		smallint, \
	 trialavg_dv		smallint, \
	 centerselfmark		smallint, \
	 center_full		smallint, \
	 ronincd		varchar(1), \
	 howtoexam1		varchar(1), \
	 howtoexam2		varchar(1), \
	 howtoexam3		varchar(1), \
	 howtoexam4		varchar(1), \
	 decision1		varchar(1), \
	 decision2		varchar(1), \
	 decision3		varchar(1), \
	 decision4		varchar(1), \
	 inoutpref1		varchar(1), \
	 inoutpref2		varchar(1), \
	 inoutpref3		varchar(1), \
	 inoutpref4		varchar(1), \
	 type1			varchar(1), \
	 type2			varchar(1), \
	 type3			varchar(1), \
	 type4			varchar(1), \
	 school1		varchar(20), \
	 school2		varchar(20), \
	 school3		varchar(20), \
	 school4		varchar(20), \
	 department1		varchar(20), \
	 department2		varchar(20), \
	 department3		varchar(20), \
	 department4		varchar(20), \
	 major1			varchar(20), \
	 major2			varchar(20), \
	 major3			varchar(20), \
	 major4			varchar(20), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table plan_stat_dat add constraint pk_planstat_dat primary key \
	(receiptyear, schregno)


