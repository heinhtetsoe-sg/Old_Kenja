
drop table menu_mst

create table menu_mst \
	(menuid		varchar(11)	not null, \
	 submenuid	varchar(1), \
	 parentmenuid	varchar(11), \
	 menuname	varchar(40), \
	 exename	varchar(20), \
	 command_arg	varchar(20), \
	 process	varchar(40), \
	 processcd	varchar(1), \
	 exe_flg	varchar(1), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table menu_mst add constraint pk_menu_mst primary key (menuid)


