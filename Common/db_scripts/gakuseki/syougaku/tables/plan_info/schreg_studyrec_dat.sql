
drop table schreg_studyrec_dat

create table schreg_studyrec_dat \
	(schoolcd		varchar(1)	not null, \
	 schregno		varchar(6)	not null, \
	 year			varchar(4), \
	 grade			varchar(1)	not null, \
	 coursecode1		varchar(3), \
	 coursecode2		varchar(3), \
	 coursecode3		varchar(3), \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 classname		varchar(20), \
	 classabbv		varchar(10), \
	 classnameenglish	varchar(40), \
	 classabbvenglish	varchar(30), \
	 subclasses		smallint, \
	 subclassname		varchar(20), \
	 subclassabbv		varchar(6), \
	 subclassnameenglish	varchar(40), \
	 subclassabbvenglish	varchar(20), \
	 grades			smallint, \
	 credit			smallint, \
	 a_credits		smallint, \
	 addcreditcd		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schreg_studyrec_dat add constraint pk_reprec_dat primary key \
	(schoolcd,schregno, grade, classcd, subclasscd)


