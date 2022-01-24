
drop table message_mst

create table message_mst \
	(msg_cd		varchar(6)	not null, \
	 msg_level	varchar(1), \
	 msg_content	varchar(100), \
	 msg_detail	varchar(100), \
	 howto		varchar(100), \
	 updated	timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table message_mst add constraint pk_message_mst primary key (msg_cd)


