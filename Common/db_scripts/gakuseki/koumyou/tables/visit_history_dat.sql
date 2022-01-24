
drop table visit_history_dat

create table visit_history_dat \
	( \
	 year		varchar(4)	not null, \
	 visit_date	date		not null, \
	 visit_time 	time		not null, \
	 senkou_kind 	varchar(1), \
	 company_cd 	varchar(8), \
	 company_name 	varchar(80), \
	 telno 	        varchar(16), \
	 visit_name 	varchar(40), \
	 visit_remark 	varchar(414), \
	 staff_cd 	varchar(6), \
	 UPDATED	timestamp default current timestamp \
	)

alter table visit_history_dat add constraint pk_visit_history primary key \
        (year, visit_date, visit_time)
