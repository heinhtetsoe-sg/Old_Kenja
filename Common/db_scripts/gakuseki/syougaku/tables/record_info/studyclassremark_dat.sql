
drop table studyclassremark_dat

create table studyclassremark_dat \
	(year			varchar(4)	not null, \
	 schregno		varchar(7)	not null, \
	 classcd		varchar(2)	not null, \
	 subclasscd		varchar(4)	not null, \
	 remark			varchar(40), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table studyclassremark_dat add constraint pk_studyclass_dat primary key \
	(year, schregno, classcd, subclasscd)


