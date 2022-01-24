
drop table plan_jobhunt_dat

create table plan_jobhunt_dat \
	( \
	 year			varchar(4)	not null, \
	 index			int			not null, \
	 schregno 		varchar(6), \
	 job_date1 		date, \
	 job_steme 		time, \
	 job_eteme 		time, \
	 stat_cd 		varchar(8), \
	 company_name 		varchar(80), \
	 shushoku_add 		varchar(80), \
	 telno	 		varchar(16), \
	 hand_date	 	date, \
	 howtoexam 		varchar(4), \
	 howtoexam_remark	varchar(80), \
	 job_remark 		varchar(80), \
	 job_content 		varchar(162), \
	 job_think 		varchar(326), \
	 jobex_date1 		date, \
	 jobex_steme 		time, \
	 jobex_eteme 		time, \
	 jobex_remark 		varchar(80), \
	 jobex_content 		varchar(162), \
	 jobex_think 		varchar(326), \
	 decision 		varchar(4), \
	 planstat 		varchar(4), \
	 UPDATED		timestamp default current timestamp \
	)

alter table plan_jobhunt_dat add constraint pk_plan_jobhunt primary key \
        (year, index)
