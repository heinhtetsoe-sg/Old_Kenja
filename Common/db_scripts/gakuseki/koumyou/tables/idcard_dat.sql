drop table idcard_dat

-- <<< idcard_dat create >>>

create table idcard_dat \
	(schregno		varchar(6)	not null, \
	 name		    varchar(40), \
	 birthday		varchar(14), \
	 address1	    varchar(50), \
	 address2	    varchar(50), \
	 validterm	    varchar(22), \
	 path   	    varchar(80) \
	) in usr1dms index in idx1dms

alter table idcard_dat add constraint pk_idcard_dat primary key \
	(schregno)


