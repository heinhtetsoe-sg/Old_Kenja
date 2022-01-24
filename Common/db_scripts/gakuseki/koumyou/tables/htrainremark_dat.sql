
drop table htrainremark_dat

create table htrainremark_dat \
	(year			varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 totalstudyact	 	varchar(358), \
	 totalstudyeval		varchar(538), \
	 specialactremark	varchar(142), \
	 totalremark		varchar(538), \
	 attendrec_remark	varchar(82), \
	 updated		timestamp default current timestamp \
	)

alter table htrainremark_dat add constraint pk_htrainremk_dat primary key \
	(year,schregno)
