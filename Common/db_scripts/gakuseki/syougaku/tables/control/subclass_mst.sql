
drop table subclass_mst

create table subclass_mst \
	(subclasscd		varchar(4)	not null, \
	 subclassname		varchar(20), \
	 subclassabbv		varchar(6), \
	 subclassnameenglish	varchar(40), \
         subclassabbvenglish    varchar(20), \
         showorder              smallint, \
	 electdiv		varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table subclass_mst add constraint pk_subclass_mst primary key (subclasscd)


