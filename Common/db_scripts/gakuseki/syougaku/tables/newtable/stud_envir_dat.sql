
drop table stud_envir_dat

create table stud_envir_dat \
	(res_year		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 howtocommutecd		varchar(1), \
	 commutehour		smallint, \
	 otherhowtocommute 	varchar(20), \
	 bro_siscd		varchar(1), \
	 residentcd		varchar(1), \
	 disease		varchar(20), \
	 healthcondition	varchar(20), \
	 merits			varchar(42), \
	 demerits		varchar(42), \
         old_cram               varchar(42), \
         cur_cramcd             varchar(1), \
         cur_cram               varchar(20), \
         lessoncd               varchar(1), \
         lesson                 varchar(20), \
         bedtime                time, \
         risingtime             time, \
         studytime              varchar(1), \
         pocketmoneycd          varchar(1), \
         pocketmoney            smallint, \
         tvviewinghourscd       varchar(1), \
         tvprogram              varchar(20), \
         pc_hours               varchar(20), \
	 good_subject		varchar(42), \
	 bad_subject		varchar(42), \
	 hobby			varchar(42), \
	 prizes			varchar(86), \
	 reading		varchar(42), \
	 sports			varchar(42), \
	 friendship		varchar(42), \
	 planuniv		varchar(42), \
	 planjob		varchar(42), \
	 ed_act			varchar(42), \
	 remark			varchar(86), \
	 updated	timestamp default current timestamp \
	)

alter table stud_envir_dat add constraint pk_stud_envir_dat primary key \
	(res_year,schregno)
