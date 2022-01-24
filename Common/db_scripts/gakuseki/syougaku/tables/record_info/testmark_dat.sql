
drop table testmark_dat

create table testmark_dat \
	(studyyear		varchar(4)	not null, \
	 studysemester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 studyclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 schregno		varchar(6)	not null, \
	 testmarkdate		date		not null, \
	 testans1		varchar(3), \
	 testans2		varchar(3), \
	 testans3		varchar(3), \
	 testans4		varchar(3), \
	 testans5		varchar(3), \
	 testans6		varchar(3), \
	 testans7		varchar(3), \
	 testans8		varchar(3), \
	 testans9		varchar(3), \
	 testans10		varchar(3), \
	 testans11		varchar(3), \
	 testans12		varchar(3), \
	 testans13		varchar(3), \
	 testans14		varchar(3), \
	 testans15		varchar(3), \
	 testans16		varchar(3), \
	 testans17		varchar(3), \
	 testans18		varchar(3), \
	 testans19		varchar(3), \
	 testans20		varchar(3), \
	 testans21		varchar(3), \
	 testans22		varchar(3), \
	 testans23		varchar(3), \
	 testans24		varchar(3), \
	 testans25		varchar(3), \
	 testans26		varchar(3), \
	 testans27		varchar(3), \
	 testans28		varchar(3), \
	 testans29		varchar(3), \
	 testans30		varchar(3), \
	 testans31		varchar(3), \
	 testans32		varchar(3), \
	 testans33		varchar(3), \
	 testans34		varchar(3), \
	 testans35		varchar(3), \
	 testans36		varchar(3), \
	 testans37		varchar(3), \
	 testans38		varchar(3), \
	 testans39		varchar(3), \
	 testans40		varchar(3), \
	 testans41		varchar(3), \
	 testans42		varchar(3), \
	 testans43		varchar(3), \
	 testans44		varchar(3), \
	 testans45		varchar(3), \
	 testans46		varchar(3), \
	 testans47		varchar(3), \
	 testans48		varchar(3), \
	 testans49		varchar(3), \
	 testans50		varchar(3), \
	 testans51		varchar(3), \
	 testans52		varchar(3), \
	 testans53		varchar(3), \
	 testans54		varchar(3), \
	 testans55		varchar(3), \
	 testans56		varchar(3), \
	 testans57		varchar(3), \
	 testans58		varchar(3), \
	 testans59		varchar(3), \
	 testans60		varchar(3), \
	 testans61		varchar(3), \
	 testans62		varchar(3), \
	 testans63		varchar(3), \
	 testans64		varchar(3), \
	 testans65		varchar(3), \
	 testans66		varchar(3), \
	 testans67		varchar(3), \
	 testans68		varchar(3), \
	 testans69		varchar(3), \
	 testans70		varchar(3), \
	 testans71		varchar(3), \
	 testans72		varchar(3), \
	 testans73		varchar(3), \
	 testans74		varchar(3), \
	 testans75		varchar(3), \
	 testans76		varchar(3), \
	 testans77		varchar(3), \
	 testans78		varchar(3), \
	 testans79		varchar(3), \
	 testans80		varchar(3), \
	 testans81		varchar(3), \
	 testans82		varchar(3), \
	 testans83		varchar(3), \
	 testans84		varchar(3), \
	 testans85		varchar(3), \
	 testans86		varchar(3), \
	 testans87		varchar(3), \
	 testans88		varchar(3), \
	 testans89		varchar(3), \
	 testans90		varchar(3), \
	 testans91		varchar(3), \
	 testans92		varchar(3), \
	 testans93		varchar(3), \
	 testans94		varchar(3), \
	 testans95		varchar(3), \
	 testans96		varchar(3), \
	 testans97		varchar(3), \
	 testans98		varchar(3), \
	 testans99		varchar(3), \
	 testans100		varchar(3), \
	 testsum		smallint, \
	 correctanswers		smallint, \
	 noticerate		dec(5,2), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table testmark_dat add constraint pk_testmark_dat primary key \
	(studyyear, studysemester, classcd, subclasscd, studyclasscd, \
	 testkindcd, testitemcd, schregno, testmarkdate)

