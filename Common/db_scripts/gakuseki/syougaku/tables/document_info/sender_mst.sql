
drop table sender_mst

create table sender_mst \
	(sendercd		varchar(2)	not null, \
	 sender_section		varchar(60), \
	 sender			varchar(60), \
	 respondsign		varchar(20), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table sender_mst add constraint pk_sender_mst primary key (sendercd) 

