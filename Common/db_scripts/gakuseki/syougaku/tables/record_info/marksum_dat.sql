
drop table marksum_dat

create table marksum_dat \
	(studyyear		varchar(4), \
	 studysemester		varchar(1), \
	 classcd		varchar(2), \
	 subclasscd		varchar(4), \
	 studyclasscd		varchar(4), \
	 testkindcd		varchar(2), \
	 testitemcd		varchar(2), \
	 testmarkdate		date, \
	 schregno		varchar(6), \
	 s_quizno		varchar(6), \
	 testanswer		varchar(3), \
	 point			smallint, \
	 testsum		smallint, \
	 correctanswers		smallint \
	) in usr1dms index in idx1dms



