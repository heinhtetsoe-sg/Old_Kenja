
drop table testbase_dat

create table testbase_dat \
	(studyyear		varchar(4)	not null, \
	 studysemester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 studyclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 testbasedate		date		not null, \
	 quizno			varchar(3)	not null, \
	 point			smallint, \
	 correct1		varchar(3), \
	 correct2		varchar(3), \
	 correct3		varchar(3), \
	 column			varchar(3), \
	 range			varchar(3), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table testbase_dat add constraint pk_testbase_dat primary key \
	(studyyear, studysemester, classcd, subclasscd, studyclasscd, \
	 testkindcd, testitemcd, testbasedate, quizno)


