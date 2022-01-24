
drop table schreg_rela_dat

create table schreg_rela_dat \
	(schregno 		varchar(6)	not null, \
	 rela_no		varchar(2)	not null, \
	 rela_lname		varchar(20), \
	 rela_fname		varchar(20), \
	 rela_lkana		varchar(40), \
	 rela_fkana		varchar(40), \
	 sex			varchar(1), \
	 birthday		date, \
 	 occupation		varchar(40), \
	 regidentialcd		varchar(2), \
	 relationship		varchar(2), \
	 rela_schregno		varchar(6), \
	 remark			varchar(30), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schreg_rela_dat add constraint pk_schreg_rela_dat primary key \
	(schregno, rela_no)


