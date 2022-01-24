
drop table class_mst

create table class_mst \
	(classcd		varchar(2)	not null, \
	 classname		varchar(20), \
	 classabbv		varchar(10), \
	 classnameenglish	varchar(40), \
	 classabbvenglish	varchar(30), \
	 subclasses		smallint, \
	 showorder		smallint, \
         electdiv		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table class_mst add constraint pk_class_mst primary key (classcd)


