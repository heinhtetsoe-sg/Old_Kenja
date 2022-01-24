
drop table medexam_tooth_dat

create table medexam_tooth_dat \
	(medexamyear		varchar(4)	not null, \
	 schregno		varchar(6)	not null, \
	 jaws_jointcd		varchar(2), \
	 plaquecd		varchar(2), \
	 gumcd			varchar(2), \
	 babytooth		smallint, \
	 remainbabytooth	smallint, \
	 treatedbabytooth	smallint, \
	 adulttooth		smallint, \
	 remainadulttooth	smallint, \
	 treatedadulttooth	smallint, \
	 lostadulttooth		smallint, \
	 otherdiseasecd		varchar(2), \
	 dentistremarkcd	varchar(2), \
	 dentistremarkdate	date, \
	 dentisttreat		varchar(20), \
	 updated	timestamp default current timestamp \
	)

alter table medexam_tooth_dat add constraint pk_medexam_tooth_d primary key \
	(medexamyear,schregno)
