
drop table certif_kind_mst

create table certif_kind_mst \
	(certif_kindcd		varchar(3)	not null, \
	 kindname		varchar(16), \
	 issuecd		varchar(1), \
	 studentcd		varchar(1), \
	 graduatecd		varchar(1), \
	 dropoutcd		varchar(1), \
	 updated 		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table certif_kind_mst add constraint pk_certkind_mst primary key \
	(certif_kindcd)


