
drop table ptaexec_dat

create table ptaexec_dat \
	(posted_year		varchar(4)	not null, \
	 grade			varchar(1)	not null, \
	 ptacd1			varchar(3)	not null, \
	 ptacd2			varchar(2), \
	 startdate		date, \
	 finishdate		date, \
	 schregno		varchar(6), \
 	 ptaexec_name		varchar(40), \
	 ptaexec_name_show	varchar(20), \
	 ptaexec_kana		varchar(10), \
	 ptaexec_zipcd		varchar(8), \
	 ptaexec_address	varchar(50), \
	 ptaexec_telno		varchar(14), \
	 remark			varchar(40), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table ptaexec_dat add constraint pk_ptaexec_dat primary key (posted_year, grade, ptacd1)

