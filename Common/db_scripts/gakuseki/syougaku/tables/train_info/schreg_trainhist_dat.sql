
drop table schreg_trainhist_dat

create table schreg_trainhist_dat \
	(year			varchar(4)      not null, \
         traindate		date	        not null, \
	 schregno		varchar(6)	not null, \
	 patientcd		varchar(2), \
	 staffcd  		varchar(6), \
	 howtotraincd		varchar(2), \
	 content		varchar(82), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schreg_trainhist_dat add constraint pk_trainhist_dat primary key \
	(year, traindate, schregno)


