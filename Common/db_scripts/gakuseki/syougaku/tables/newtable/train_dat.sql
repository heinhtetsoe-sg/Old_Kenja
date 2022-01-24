
drop table train_dat

create table train_dat \
	(year			varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 trainterm1		varchar(1), \
	 trainterm2		varchar(1), \
	 trainterm3		varchar(1), \
	 trainyear		varchar(1), \
	 princ_trainterm1	varchar(1), \
	 princ_trainterm2	varchar(1), \
	 princ_trainterm3	varchar(1), \
	 princ_trainyear	varchar(1), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table train_dat add constraint pk_train_dat primary key \
	(year, schregno)
