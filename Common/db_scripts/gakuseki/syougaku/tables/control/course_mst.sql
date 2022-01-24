
drop table course_mst

create table course_mst \
	(coursecd		varchar(1) 	not null, \
	 coursename		varchar(8), \
	 courseabbv		varchar(4), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table course_mst add constraint pk_course_mst primary key (coursecd)


