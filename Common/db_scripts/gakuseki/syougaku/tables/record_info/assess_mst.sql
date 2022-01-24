
drop table assess_mst

create table assess_mst \
	(assesscd		varchar(1)	not null, \
	 assesslevel		smallint	not null, \
	 assesslow		dec(4,1), \
	 assesshigh		dec(4,1), \
	 updated		timestamp default current timestamp \
      	) in usr1dms index in idx1dms

alter table assess_mst add constraint pk_assess_mst primary key \
	(assesscd, assesslevel)


