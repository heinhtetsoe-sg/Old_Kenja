
drop table schreg_award_dat

create table schreg_award_dat \
	(year			varchar(4)      not null, \
         regddate		date	        not null, \
	 schregno		varchar(6)	not null, \
         seqno  		smallint	not null, \
	 awardcd		varchar(2), \
	 qualifiedcd  		varchar(2), \
	 penaltycd		varchar(2), \
	 content		varchar(600), \
	 remark			varchar(40), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table schreg_award_dat add constraint pk_sraward_dat primary key \
	(year, regddate, schregno, seqno)
