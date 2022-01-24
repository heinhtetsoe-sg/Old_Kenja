
drop table htrainremark_hdat

create table htrainremark_hdat \
	(schregno		varchar(6)	not null, \
	 totalstudyact	 	varchar(358), \
	 totalstudyeval		varchar(538), \
	 updated		timestamp default current timestamp \
	)

alter table htrainremark_hdat add constraint pk_htrainremk_hdat primary key \
	(schregno)
