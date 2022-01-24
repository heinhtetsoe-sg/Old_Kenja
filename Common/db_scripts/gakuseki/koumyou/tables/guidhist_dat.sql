
drop table guidhist_dat

create table guidhist_dat \
	( \
	 regddate	date		not null, \
	 year		varchar(4)	not null, \
	 schregno 	varchar(6)	not null, \
	 regdtime 	time, \
	 postion 	varchar(20), \
	 disposal 	varchar(20), \
	 perioddate1 	date, \
	 perioddate2 	date, \
	 content 	varchar(206), \
	 guidance 	varchar(206), \
	 remark 	varchar(206), \
	 semester 	varchar(1), \
	 grade 		varchar(1), \
	 hr_class 	varchar(2), \
	 attendno 	varchar(2), \
	 tr_cd1 	varchar(6), \
	 UPDATED	timestamp default current timestamp \
	)

alter table guidhist_dat add constraint pk_guidhist_dat primary key \
        (regddate, year, schregno)
