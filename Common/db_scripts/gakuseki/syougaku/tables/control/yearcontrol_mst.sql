
drop table yearcontrol_mst

create table yearcontrol_mst \
	(ctrl_year	varchar(4)	not null, \
	 ctrl_cd1	varchar(4)	not null, \
	 ctrl_cd2	varchar(4)	not null, \
	 ctrl_char1	varchar(20), \
	 ctrl_char2	varchar(20), \
	 ctrl_char3	varchar(20), \
	 ctrl_char4	varchar(20), \
	 ctrl_char5	varchar(20), \
	 ctrl_value1	smallint, \
	 ctrl_value2	smallint, \
	 ctrl_value3	smallint, \
	 ctrl_value4	smallint, \
	 ctrl_value5	smallint, \
	 ctrl_date1	timestamp, \
	 ctrl_date2	timestamp, \
	 ctrl_date3	timestamp, \
	 ctrl_date4	timestamp, \
	 ctrl_date5	timestamp, \
	 ctrl_howto	varchar(100), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table yearcontrol_mst add constraint pk_yearctrl_mst primary key \
	(ctrl_year, ctrl_cd1, ctrl_cd2)


	
