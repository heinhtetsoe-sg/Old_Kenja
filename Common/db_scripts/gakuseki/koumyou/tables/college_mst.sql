
drop table college_mst

create table college_mst \
	( \
	 SCHOOL_CD	varchar(8)	not null, \
	 SCHOOL_NAME 	varchar(80), \
	 BUNAME 	varchar(80), \
	 KANAME 	varchar(80), \
	 SCHOOL_SORT 	varchar(1), \
	 BUNYA 		varchar(1), \
	 SHOZAITH 	varchar(20), \
	 ZIPCD 		varchar(8), \
	 ADDRESS1 	varchar(60), \
	 ADDRESS2 	varchar(60), \
	 TELNO 		varchar(16), \
	 GREDES 	varchar(80), \
	 UPDATED	timestamp default current timestamp \
	)

alter table college_mst add constraint pk_college_mst primary key \
        (SCHOOL_CD)
