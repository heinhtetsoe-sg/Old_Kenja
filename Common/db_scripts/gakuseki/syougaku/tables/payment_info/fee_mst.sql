
drop table fee_mst

create table fee_mst \
	(majorcd		varchar(3)	not null, \
	 feeyear		varchar(4)	not null, \
	 coursecd		varchar(1)	not null, \
	 grade			varchar(1)	not null, \
	 feecd			varchar(1)	not null, \
	 fee			smallint, \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table fee_mst add constraint pk_fee_mst primary key \
	(majorcd, feeyear, coursecd, grade, feecd)


