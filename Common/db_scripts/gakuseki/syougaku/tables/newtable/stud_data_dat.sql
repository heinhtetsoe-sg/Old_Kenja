
drop table stud_data_dat

create table stud_data_dat \
	(res_year		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 commuteareacd		varchar(1), \
	 howtocommutecd		varchar(1), \
	 otherhowtocommute 	varchar(20), \
	 bro_siscd		varchar(1), \
	 residentcd		varchar(1), \
	 guard_occupation	varchar(20), \
	 guard_occupationcd	varchar(2), \
	 disease		varchar(20), \
	 healthcondition	varchar(30), \
	 remark			varchar(30), \
	 tb			varchar(10), \
	 unbalanced		varchar(1), \
	 old_cram		varchar(40), \
	 cur_cramcd		varchar(1), \
	 cur_cram		varchar(20), \
	 lessoncd		varchar(1), \
	 lesson			varchar(10), \
	 bedtime		time, \
	 risingtime		time, \
	 studytime		varchar(1), \
	 pocketmoneycd		varchar(1), \
	 pocketmoney		smallint, \
	 tvviewinghourscd	varchar(1), \
	 tvprogram		varchar(20), \
	 pc_hours		varchar(1), \
	 goodsubject		varchar(20), \
	 badsubject		varchar(20), \
	 homestudycd		varchar(1), \
	 homestudyhours		dec(2,1), \
	 jobplan1		varchar(20), \
	 univplan1		varchar(20), \
	 jobplan2		varchar(20), \
	 univplan2		varchar(20), \
	 hobby			varchar(40), \
	 request		varchar(60), \
	 pre_exec		varchar(20), \
	 pre_award		varchar(20), \
	 pre_repre		varchar(40), \
	 pre_duty		varchar(40), \
	 pre_homefriend		varchar(40), \
	 pre_schoolfriend	varchar(40), \
	 updated	timestamp default current timestamp \
	)

alter table stud_data_dat add constraint pk_stud_data_dat primary key \
	(res_year,schregno)
