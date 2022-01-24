
drop table company_mst

create table company_mst \
	( \
	 COMPANY_CD	varchar(8)	not null, \
	 COMPANY_NAME 	varchar(80), \
	 SHUSHOKU_ADD 	varchar(80), \
	 SIHONKIN 	varchar(17), \
	 SONINZU 	dec(8,0), \
	 TONINZU 	dec(8,0), \
	 COMPANY_SORT 	varchar(2), \
	 TAISHOU 	varchar(1), \
	 ZIPCD 		varchar(8), \
	 ADDRESS1 	varchar(60), \
	 ADDRESS2 	varchar(60), \
	 TELNO 		varchar(16), \
	 REMARK 	varchar(60), \
	 UPDATED	timestamp default current timestamp \
	)

alter table company_mst add constraint pk_company_mst primary key \
        (COMPANY_CD)
