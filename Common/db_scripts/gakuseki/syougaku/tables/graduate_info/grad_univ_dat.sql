
drop table grad_univ_dat

create table grad_univ_dat \
	(schregno		varchar(6)	not null, \
	 univ_year		varchar(4), \
	 centerselfmark		smallint, \
	 center_full		smallint, \
	 pass_flg		varchar(1), \
	 howtoexam		varchar(1), \
	 inoutprefcd		varchar(1), \
	 typecd			varchar(1), \
	 school			varchar(20), \
	 department		varchar(20), \
	 major			varchar(20), \
	 multipass_flg		varchar(1), \
	 howtomultiexam		varchar(1), \
	 multiinoutprefcd	varchar(1), \
	 multi_typecd		varchar(1), \
	 multischool		varchar(20), \
	 multidepartment	varchar(20), \
	 multimajor		varchar(20), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table grad_univ_dat add constraint pk_grad_univ primary key (schregno)		
