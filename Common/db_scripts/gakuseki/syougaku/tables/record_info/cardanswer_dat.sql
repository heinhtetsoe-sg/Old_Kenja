
drop table cardanswer_dat

create table cardanswer_dat \
	(studyyear		varchar(4)	not null, \
	 studysemester		varchar(1)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd	  	varchar(4)	not null, \
	 studyclasscd		varchar(4)	not null, \
	 testkindcd		varchar(2)	not null, \
	 testitemcd		varchar(2)	not null, \
	 schregno		varchar(6)	not null, \
	 cardtestdate		date		not null, \
	 column1		varchar(3), \
	 column2		varchar(3), \
	 column3		varchar(3), \
	 column4		varchar(3), \
	 column5		varchar(3), \
	 column6		varchar(3), \
	 column7		varchar(3), \
	 column8		varchar(3), \
	 column9		varchar(3), \
	 column10		varchar(3), \
	 column11		varchar(3), \
	 column12		varchar(3), \
	 column13		varchar(3), \
	 column14		varchar(3), \
	 column15		varchar(3), \
	 column16		varchar(3), \
	 column17		varchar(3), \
	 column18		varchar(3), \
	 column19		varchar(3), \
	 column20		varchar(3), \
	 column21		varchar(3), \
	 column22		varchar(3), \
	 column23		varchar(3), \
	 column24		varchar(3), \
	 column25		varchar(3), \
	 column26		varchar(3), \
	 column27		varchar(3), \
	 column28		varchar(3), \
	 column29		varchar(3), \
	 column30		varchar(3), \
	 column31		varchar(3), \
	 column32		varchar(3), \
	 column33		varchar(3), \
	 column34		varchar(3), \
	 column35		varchar(3), \
	 column36		varchar(3), \
	 column37		varchar(3), \
	 column38		varchar(3), \
	 column39		varchar(3), \
	 column40		varchar(3), \
	 column41		varchar(3), \
	 column42		varchar(3), \
	 column43		varchar(3), \
	 column44		varchar(3), \
	 column45		varchar(3), \
	 column46		varchar(3), \
	 column47		varchar(3), \
	 column48		varchar(3), \
	 column49		varchar(3), \
	 column50		varchar(3), \
	 column51		varchar(3), \
	 column52		varchar(3), \
	 column53		varchar(3), \
	 column54		varchar(3), \
	 column55		varchar(3), \
	 column56		varchar(3), \
	 column57		varchar(3), \
	 column58		varchar(3), \
	 column59		varchar(3), \
	 column60		varchar(3), \
	 column61		varchar(3), \
	 column62		varchar(3), \
	 column63		varchar(3), \
	 column64		varchar(3), \
	 column65		varchar(3), \
	 column66		varchar(3), \
	 column67		varchar(3), \
	 column68		varchar(3), \
	 column69		varchar(3), \
	 column70		varchar(3), \
	 column71		varchar(3), \
	 column72		varchar(3), \
	 column73		varchar(3), \
	 column74		varchar(3), \
	 column75		varchar(3), \
	 column76		varchar(3), \
	 column77		varchar(3), \
	 column78		varchar(3), \
	 column79		varchar(3), \
	 column80		varchar(3), \
	 column81		varchar(3), \
	 column82		varchar(3), \
	 column83		varchar(3), \
	 column84		varchar(3), \
	 column85		varchar(3), \
	 column86		varchar(3), \
	 column87		varchar(3), \
	 column88		varchar(3), \
	 column89		varchar(3), \
	 column90		varchar(3), \
	 column91		varchar(3), \
	 column92		varchar(3), \
	 column93		varchar(3), \
	 column94		varchar(3), \
	 column95		varchar(3), \
	 column96		varchar(3), \
	 column97		varchar(3), \
	 column98		varchar(3), \
	 column99		varchar(3), \
	 column100		varchar(3), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table cardanswer_dat add constraint pk_cardanswer_dat primary key \
	(studyyear, studysemester, classcd, subclasscd, studyclasscd, \
	 testkindcd, testitemcd, schregno, cardtestdate)


