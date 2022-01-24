
drop table trainremark_dat

create table trainremark_dat \
	(year		varchar(4) 	not null, \
	 schregno	varchar(6)	not null, \
	 studyact	varchar(120), \
	 view		varchar(40), \
	 grades		varchar(120), \
	 totalremark	varchar(500), \
	 attendremark	varchar(40), \
	 updated	timestamp default current timestamp \
	)

alter table trainremark_dat add constraint pk_trainremark_dat primary key \
	(year,schregno)
