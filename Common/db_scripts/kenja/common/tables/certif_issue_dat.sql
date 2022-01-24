-- $Id: 5fec51be2a5e722055079e0f6d4ed041e1b76d5b $

drop table certif_issue_dat

create table certif_issue_dat \
	(year		varchar(4)	not null, \
	 certif_index		varchar(5)	not null, \
	 schregno		varchar(8), \
	 certif_kindcd		varchar(3), \
	 graduate_flg		varchar(1), \
	 applydate		date, \
	 issuername		varchar(40), \
	 issuecd		varchar(1), \
	 certif_no		smallint, \
	 issuedate		date, \
	 charge			varchar(1), \
 	 printcd		varchar(1), \
	 registercd		varchar(8), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table certif_issue_dat add constraint pk_certissue_dat primary key \
	(year, certif_index)


