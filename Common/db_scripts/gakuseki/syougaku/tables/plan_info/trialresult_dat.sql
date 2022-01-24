
drop table trialresult_dat

create table trialresult_dat \
	(trialyear		varchar(4)	not null, \
	 trialdate		date		not null, \
	 trialsemester		varchar(1)	not null, \
	 trialgrade		varchar(2)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 schregno		varchar(6)	not null, \
	 totalrank		smallint, \
	 totalscore		smallint, \
	 total_dv		dec(5,2), \
	 schoolrank		smallint, \
	 school_dv		dec(5,2), \
	 aj_dv			smallint, \
	 std_score		smallint, \
	 class_dv		dec(5,2), \
	 classrank		smallint, \
	 japaneserank		smallint, \
	 japanesescore		smallint, \
	 japanese_dv		dec(5,2), \
	 alljapaneserank	smallint, \
	 alljapanese_dv		smallint, \
	 classjapanese_dv	dec(5,2), \
	 classjapaneserank	smallint, \
	 mathrank		smallint, \
	 mathscore		smallint, \
	 math_dv		dec(5,2), \
	 allmathrank		smallint, \
	 allmath_dv		smallint, \
	 classmath_dv		dec(5,2), \
	 classmathrank		smallint, \
	 englishrank		smallint, \
	 englishscore		smallint, \
	 english_dv		dec(5,2), \
	 allenglishrank		smallint, \
	 allenglish_dv		smallint, \
	 classenglish_dv	dec(5,2), \
	 classenglishrank	smallint, \
	 societyrank		smallint, \
	 societyscore		smallint, \
	 society_dv		dec(5,2), \
	 allsocietyrank		smallint, \
	 allsociety_dv		smallint, \
	 classsociety_dv	dec(5,2), \
	 classsocietyrank	smallint, \
	 sciencerank		smallint, \
	 sciencescore		smallint, \
	 science_dv		dec(5,2), \
	 allsciencerank		smallint, \
	 allscience_dv		smallint, \
	 classscience_dv	dec(5,2), \
	 classsciencerank	smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table trialresult_dat add constraint pk_trires_dat primary key \
	(trialyear, trialdate, trialsemester, trialgrade,  \
	 testkindcd, testitemcd, schregno)


