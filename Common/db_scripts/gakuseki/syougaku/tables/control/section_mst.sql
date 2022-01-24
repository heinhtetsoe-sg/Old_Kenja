
drop table section_mst

create table section_mst \
	(sectioncd		varchar(4)	not null, \
	 sectionname		varchar(16), \
	 sectionabbv		varchar(8), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table section_mst add constraint pk_section_mst primary key \
	(sectioncd)


