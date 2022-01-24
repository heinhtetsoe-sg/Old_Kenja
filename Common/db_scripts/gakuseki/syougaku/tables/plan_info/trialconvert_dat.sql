
drop table trialconvert_dat

create table trialconvert_dat \
	(trialyear		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 grade			varchar(1), \
 	 hr_class		varchar(2), \
	 no			varchar(2), \
	 name			varchar(40), \
	 totalscore		smallint, \
	 total_full		smallint, \
	 total_dv		dec(5,2), \
	 japanesescore		smallint, \
	 japanese_full		smallint, \
	 japanese_dv		dec(5,2), \
	 mathscore		smallint,  \
	 math_full		smallint, \
	 math_dv		dec(5,2), \
	 englishscore		smallint, \
	 english_full		smallint, \
	 english_dv		dec(5,2), \
	 sciencescore		smallint, \
	 science_full		smallint, \
	 science_dv		dec(5,2), \
	 societyscore		smallint, \
	 society_full		smallint, \
	 society_dv		dec(5,2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table trialconvert_dat add constraint pk_tricon_dat primary key \
	(trialyear, schregno)



