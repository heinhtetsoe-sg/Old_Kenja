
drop table shamexamination_dat

create table shamexamination_dat \
	(year           varchar(4)	not null, \
	 shamexamcd     varchar(2) 	not null, \
	 schregno       varchar(6) 	not null, \
	 subclasscd     varchar(4)	not null, \
	 subclassname   varchar(20), \
	 score          smallint, \
	 deviation      smallint, \
	 updated        timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table shamexamination_dat add constraint pk_shamexam_dat primary key (year, shamexamcd, schregno, subclasscd)


