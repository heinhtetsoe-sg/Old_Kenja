
drop table staff_mst

create table staff_mst \
	(staffcd			varchar(6) 	not null, \
	 lname				varchar(20), \
	 fname				varchar(20), \	 
	 lname_show			varchar(10), \
	 fname_show			varchar(10), \
	 lname_kana			varchar(40), \
	 fname_kana			varchar(40), \
	 lname_eng			varchar(20), \
	 fname_eng			varchar(20), \
	 staffbirthday			date, \
	 staffsex			varchar(1), \
	 staffzipcd			varchar(8), \
	 staffaddress1			varchar(50), \
	 staffaddress2			varchar(50), \
	staffhometowncd			varchar(5), \
	 stafftelno			varchar(14), \
	 faxno				varchar(14), \
	 e_mail				varchar(14), \
	 updated			timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table staff_mst add constraint pk_staff_mst primary key (staffcd)


