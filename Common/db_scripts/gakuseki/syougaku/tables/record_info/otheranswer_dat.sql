
drop table otheranswer_dat

create table otheranswer_dat \
	(studyyear		varchar(4)	not null, \
	 studysemester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 studyclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 testbasedate		date		not null, \
	 staffcd		varchar(6)	not null, \
	 otheranswerdate	date		not null, \
	 questionno		varchar(5)	not null, \
	 quizno			varchar(3)	not null, \
	 otheranswer		varchar(5), \
	 updated 		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table otheranswer_dat add constraint pk_otherans_dat primary key \
	(studyyear, studysemester, classcd, subclasscd, studyclasscd, \
	 testkindcd, testitemcd, testbasedate, staffcd, otheranswerdate, \
	 questionno, quizno)


