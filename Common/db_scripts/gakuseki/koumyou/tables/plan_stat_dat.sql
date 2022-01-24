
drop table plan_stat_dat

create table plan_stat_dat \
	( \
	 year			varchar(4)	not null, \
	 stat_cd 		varchar(8)	not null, \
	 schregno 		varchar(6)	not null, \
	 stat_date1 		date, \
	 stat_steme 		time, \
	 stat_eteme 		time, \
	 stat_date2 		date, \
	 howtoexam 		varchar(4), \
	 howtoexam_remark	varchar(80), \
	 contentexam 		varchar(80), \
	 reasonexam 		varchar(162), \
	 thinkexam 		varchar(326), \
	 decision 		varchar(4), \
	 planstat 		varchar(4), \
	 UPDATED		timestamp default current timestamp \
	)

alter table plan_stat_dat add constraint pk_plan_stat_dat primary key \
        (year, stat_cd, schregno)
