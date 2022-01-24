
drop table control_mst

create table control_mst \
	(ctrl_cd1		varchar(4)	not null, \
	 ctrl_cd2		varchar(4)	not null, \
	 ctrl_char1		varchar(20), \
	 ctrl_char2		varchar(20), \
	 ctrl_char3		varchar(20), \
	 ctrl_char4		varchar(20), \
	 ctrl_char5		varchar(20), \
	 ctrl_value1		integer, \
	 ctrl_value2		integer, \
	 ctrl_value3		integer, \
	 ctrl_value4		integer, \
	 ctrl_value5		integer, \
	 ctrl_date1		timestamp, \
	 ctrl_date2		timestamp, \
	 ctrl_date3		timestamp, \
	 ctrl_date4		timestamp, \
	 ctrl_date5		timestamp, \
	 ctrl_char1memo		varchar(40), \
	 ctrl_char2memo		varchar(40), \
	 ctrl_char3memo		varchar(40), \
	 ctrl_char4memo		varchar(40), \
	 ctrl_char5memo		varchar(40), \
	 ctrl_value1memo	varchar(40), \
	 ctrl_value2memo	varchar(40), \
	 ctrl_value3memo	varchar(40), \
	 ctrl_value4memo	varchar(40), \
	 ctrl_value5memo	varchar(40), \
	 ctrl_date1memo		varchar(40), \
	 ctrl_date2memo		varchar(40), \
	 ctrl_date3memo		varchar(40), \
	 ctrl_date4memo		varchar(40), \
	 ctrl_date5memo		varchar(40), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table control_mst add constraint pk_control_mst primary key \
	(ctrl_cd1, ctrl_cd2)


