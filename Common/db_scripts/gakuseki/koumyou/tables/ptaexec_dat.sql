
drop table ptaexec_dat

create table ptaexec_dat \
	( \
	 posted_year 		varchar(4) not null, \
	 ptaflg			varchar(1) not null, \
	 sch_staf_no 		varchar(6) not null, \
	 grade 			varchar(1), \
	 hr_class 		varchar(2), \
	 ptacd1 		varchar(2), \
	 ptacd2 		varchar(2), \
	 startdate 		date, \
	 finishdate 		date, \
	 ptaexec_lname 		varchar(20), \
	 ptaexec_fname 		varchar(20), \
	 ptaexec_lname_show 	varchar(10), \
	 ptaexec_fname_show 	varchar(10), \
	 ptaexec_lkana	 	varchar(40), \
	 ptaexec_fkana 		varchar(40), \
	 ptaexec_zipcd 		varchar(8), \
	 ptaexec_address1 	varchar(50), \
	 ptaexec_address2 	varchar(50), \
	 ptaexec_telno 		varchar(14), \
	 ptaexec_faxno 		varchar(14), \
	 ptaexec_e_mail 	varchar(20), \
	 ptaexec_remark 	varchar(40), \
	 UPDATED	timestamp default current timestamp \
	)

alter table ptaexec_dat add constraint pk_ptaexec_dat primary key \
        (posted_year, ptaflg, sch_staf_no)
