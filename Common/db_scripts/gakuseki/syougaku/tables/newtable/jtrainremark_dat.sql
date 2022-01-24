
drop table jtrainremark_dat

create table jtrainremark_dat \
	(year		varchar(4) 	not null, \
	 schregno	varchar(6)	not null, \
	 studyact	varchar(166), \
	 view		varchar(86), \
	 grades		varchar(166), \
	 totalremark	varchar(926), \
	 attendremark	varchar(40), \
	 updated	timestamp default current timestamp \
	)

alter table jtrainremark_dat add constraint pk_trainremark_dat primary key \
	(year,schregno)
