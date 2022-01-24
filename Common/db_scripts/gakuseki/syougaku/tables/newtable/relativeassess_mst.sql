
drop table relativeassess_mst

create table relativeassess_mst \
	(grade			varchar(1)	not null, \
	 subclasscd		varchar(4)	not null, \
	 assesscd		varchar(1)	not null, \
	 assesslevel		smallint	not null, \
	 assesslow		smallint, \
	 assesshigh		smallint, \
	 updated		timestamp default current timestamp \
      	) in usr1dms index in idx1dms

alter table relativeassess_mst add constraint pk_relaasses_mst primary key \
	(grade,subclasscd,assesscd,assesslevel)


