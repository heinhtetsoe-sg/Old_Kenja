
drop table medexam_hdat

create table medexam_hdat \
	(medexamyear	varchar(4)	not null, \
	 schregno	varchar(6)	not null, \
	 date		date, \
	 updated	timestamp default current timestamp \
	)

alter table medexam_hdat add constraint pk_medexam_hdat primary key \
	(medexamyear,schregno)
