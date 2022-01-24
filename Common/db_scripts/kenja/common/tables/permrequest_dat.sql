-- $Id: a5a68891b36a7e58f5016b814cd6e73f1e3d970b $

drop table permrequest_dat

create table permrequest_dat \
	(applyday	date 		not null, \
	 applycd	varchar(1)	not null, \
	 staffcd	varchar(8)	not null, \
	 sdate		timestamp	not null, \
	 edate		timestamp	not null, \
	 hours		varchar(2), \
	 minutes	varchar(2), \
	 vacation	varchar(372), \
	 vacationreason	varchar(372), \
	 guide		varchar(93), \
	 guide_num	smallint, \
	 businesstrip	varchar(186), \
	 remark		varchar(153), \
	 call_name	varchar(30), \
	 call_telno	varchar(14), \
	 perm_cd	varchar(1), \
	 perm_staffcd	varchar(8), \
	 REGISTERCD	VARCHAR	(8), \
	 updated	timestamp default current timestamp \
	)

alter table permrequest_dat add constraint pk_permrequest_dat primary key \
	(applyday,applycd,staffcd,sdate,edate)
