
create table medexam_rireki_dat \
	(medexamyear	varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 mizyuken_cd	varchar(2)	not null, \
	 issve_date		date, \
	 issve_cd		varchar(2), \
	 remark			varchar(300), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table medexam_rireki_dat add constraint pk_med_rir_dat primary key \
	(medexamyear,schregno,mizyuken_cd)


