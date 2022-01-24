
drop table trialresult_hdat

create table trialresult_hdat \
	(trialyear		varchar(4)	not null, \
	 trialdate		date		not null, \
	 trialsemester		varchar(1)	not null, \
	 trialgrade		varchar(2)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 trialname		varchar(20), \
	 total_num		smallint, \
	 total_avg		smallint, \
	 total_full		smallint, \
	 school_num		smallint, \
	 school_avg		smallint, \
	 japanese_num		smallint, \
	 japanese_avg		smallint, \
	 japanese_full		smallint, \
	 aj_japanese_num	smallint, \
	 aj_japanese_avg	smallint, \
	 math_num		smallint, \
	 math_avg		smallint, \
	 math_full		smallint, \
	 aj_math_num		smallint, \
	 aj_math_avg		smallint, \
	 english_num		smallint, \
	 english_avg		smallint, \
	 english_full		smallint, \
	 aj_english_num		smallint, \
	 aj_english_avg		smallint, \
	 society_num		smallint, \
	 society_avg		smallint, \
	 society_full		smallint, \
	 aj_society_num		smallint, \
	 aj_society_avg		smallint, \
	 science_num		smallint, \
	 science_avg		smallint, \
	 science_full		smallint, \
	 aj_science_num		smallint, \
	 aj_science_avg		smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table trialresult_hdat add constraint pk_trires_hdat primary key \
	(trialyear, trialdate, trialsemester, trialgrade, testkindcd, testitemcd)


