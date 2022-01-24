
drop table testsum_dat

create table testsum_dat \
	(studyyear		varchar(4)	not null, \
	 studysemester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 studyclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 l_quizno		varchar(3)	not null, \
	 s_quizno		varchar(3)	not null, \
	 noticerate		dec(5,2), \
	 correctanswers		smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table testsum_dat add constraint pk_testsum_dat primary key \
	(studyyear, studysemester, classcd, subclasscd, studyclasscd, \
	 testkindcd, testitemcd, l_quizno, s_quizno)



