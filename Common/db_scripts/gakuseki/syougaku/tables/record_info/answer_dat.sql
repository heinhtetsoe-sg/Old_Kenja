
drop table answer_dat

create table answer_dat \
	(studyyear		varchar(4)	not null, \
	 studysemester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 studyclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 schregno		varchar(6)	not null, \
	 testmarkdate		date		not null, \
	 answer1		varchar(3), \
	 answer2		varchar(3), \
	 answer3		varchar(3), \
	 answer4		varchar(3), \
	 answer5		varchar(3), \
	 answer6		varchar(3), \
	 answer7		varchar(3), \
	 answer8		varchar(3), \
	 answer9		varchar(3), \
	 answer10		varchar(3), \
	 answer11		varchar(3), \
	 answer12		varchar(3), \
	 answer13		varchar(3), \
	 answer14		varchar(3), \
	 answer15		varchar(3), \
	 answer16		varchar(3), \
	 answer17		varchar(3), \
	 answer18		varchar(3), \
	 answer19		varchar(3), \
	 answer20		varchar(3), \
	 answer21		varchar(3), \
	 answer22		varchar(3), \
	 answer23		varchar(3), \
	 answer24		varchar(3), \
	 answer25		varchar(3), \
	 answer26		varchar(3), \
	 answer27		varchar(3), \
	 answer28		varchar(3), \
	 answer29		varchar(3), \
	 answer30		varchar(3), \
	 answer31		varchar(3), \
	 answer32		varchar(3), \
	 answer33		varchar(3), \
	 answer34		varchar(3), \
	 answer35		varchar(3), \
	 answer36		varchar(3), \
	 answer37		varchar(3), \
	 answer38		varchar(3), \
	 answer39		varchar(3), \
	 answer40		varchar(3), \
	 answer41		varchar(3), \
	 answer42		varchar(3), \
	 answer43		varchar(3), \
	 answer44		varchar(3), \
	 answer45		varchar(3), \
	 answer46		varchar(3), \
	 answer47		varchar(3), \
	 answer48		varchar(3), \
	 answer49		varchar(3), \
	 answer50		varchar(3), \
	 answer51		varchar(3), \
	 answer52		varchar(3), \
	 answer53		varchar(3), \
	 answer54		varchar(3), \
	 answer55		varchar(3), \
	 answer56		varchar(3), \
	 answer57		varchar(3), \
	 answer58		varchar(3), \
	 answer59		varchar(3), \
	 answer60		varchar(3), \
	 answer61		varchar(3), \
	 answer62		varchar(3), \
	 answer63		varchar(3), \
	 answer64		varchar(3), \
	 answer65		varchar(3), \
	 answer66		varchar(3), \
	 answer67		varchar(3), \
	 answer68		varchar(3), \
	 answer69		varchar(3), \
	 answer70		varchar(3), \
	 answer71		varchar(3), \
	 answer72		varchar(3), \
	 answer73		varchar(3), \
	 answer74		varchar(3), \
	 answer75		varchar(3), \
	 answer76		varchar(3), \
	 answer77		varchar(3), \
	 answer78		varchar(3), \
	 answer79		varchar(3), \
	 answer80		varchar(3), \
	 answer81		varchar(3), \
	 answer82		varchar(3), \
	 answer83		varchar(3), \
	 answer84		varchar(3), \
	 answer85		varchar(3), \
	 answer86		varchar(3), \
	 answer87		varchar(3), \
	 answer88		varchar(3), \
	 answer89		varchar(3), \
	 answer90		varchar(3), \
	 answer91		varchar(3), \
	 answer92		varchar(3), \
	 answer93		varchar(3), \
	 answer94		varchar(3), \
	 answer95		varchar(3), \
	 answer96		varchar(3), \
	 answer97		varchar(3), \
	 answer98		varchar(3), \
	 answer99		varchar(3), \
	 answer100		varchar(3), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table answer_dat add constraint pk_answer_dat primary key \
	(studyyear, studysemester, classcd, subclasscd, studyclasscd, \
	 testkindcd, testitemcd, schregno, testmarkdate)

