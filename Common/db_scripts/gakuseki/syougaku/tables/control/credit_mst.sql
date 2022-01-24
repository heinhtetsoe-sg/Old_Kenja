
drop table credit_mst

create table credit_mst \
	(year			varchar(4)	not null, \
	 coursecd		varchar(1)	not null, \
	 majorcd		varchar(3)	not null, \
	 grade			varchar(1)	not null, \
	 coursecode1		varchar(3)	not null, \
	 coursecode2		varchar(3)	not null, \
	 coursecode3		varchar(3)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 credits		smallint, \
	 require_flg		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table credit_mst add constraint pk_credit_mst primary key \
	(year,coursecd, majorcd, grade, coursecode1,coursecode2,coursecode3, classcd, subclasscd)
