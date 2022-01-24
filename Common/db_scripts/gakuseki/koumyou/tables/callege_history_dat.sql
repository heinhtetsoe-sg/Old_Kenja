
drop table callege_history_dat

create table callege_history_dat \
	( \
	 year		varchar(4)	not null, \
	 explan_date 	date		not null, \
	 explan_time 	time		not null, \
	 callege_cd 	varchar(8), \
	 school_name 	varchar(80), \
	 telno 	        varchar(16), \
	 explan_remark 	varchar(414), \
	 staff_cd 	varchar(6), \
	 UPDATED		timestamp default current timestamp \
	)

alter table callege_history_dat add constraint pk_callege_hist primary key \
        (year, explan_date, explan_time)
