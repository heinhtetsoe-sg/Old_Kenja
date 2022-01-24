
drop table permrequest_dat

create table permrequest_dat \
	(applyday	date 		not null, \
	 applycd	varchar(1)	not null, \
	 staffcd	varchar(6)	not null, \
	 sdate		timestamp	not null, \
	 fdate		timestamp	not null, \
	 hours		varchar(2), \
	 minutes	varchar(2), \
	 vacation	varchar(248), \
	 vacationreason	varchar(248), \
	 guide		varchar(62), \
	 guide_num	smallint, \
	 businesstrip	varchar(124), \
	 remark		varchar(102), \
	 call_name	varchar(20), \
	 call_telno	varchar(14), \
	 perm_cd	varchar(1), \
	 perm_staffcd	varchar(6), \
	 updated	timestamp default current timestamp \
	)

alter table permrequest_dat add constraint pk_permrequest_dat primary key \
	(applyday,applycd,staffcd,sdate,fdate)
