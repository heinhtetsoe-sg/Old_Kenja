
drop table club_dispatch_dat

create table club_dispatch_dat \
 	(clubcd			varchar(4)	not null, \
	 dp_sdate		date		not null, \
	 schregno		varchar(6)	not null, \
	 dp_remark		varchar(100), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table club_dispatch_dat add constraint pk_clubd_dat primary key \
	(clubcd, dp_sdate, schregno)



