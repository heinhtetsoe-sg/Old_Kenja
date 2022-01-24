
drop table zipcd_mst

create table zipcd_mst \
	(zipno		varchar(10)	not null, \
	 new_zipcd	varchar(8), \
	 old_zipcd	varchar(6), \
	 pref		varchar(8), \
	 citycd		varchar(5), \
	 city		varchar(32), \
	 city_kana	varchar(32), \
	 town		varchar(32), \
	 town_kana	varchar(32), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table zipcd_mst add constraint pk_zipcd_mst primary key (zipno)

