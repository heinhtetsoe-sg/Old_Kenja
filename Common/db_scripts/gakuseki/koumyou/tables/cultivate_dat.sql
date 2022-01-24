
drop table cultivate_dat

create table cultivate_dat \
	( \
	 entey_year	varchar(4)	not null, \
	 entey_index	varchar(4)	not null, \
	 inoutcd 	varchar(1), \
	 lname 		varchar(20), \
	 fname 		varchar(20), \
	 lkana 		varchar(40), \
	 fkana 		varchar(40), \
	 birthday 	date, \
	 sex 		varchar(1), \
	 zipcd 		varchar(8), \
	 address1 	varchar(50), \
	 address2 	varchar(50), \
	 telno	 	varchar(14), \
	 schregno 	varchar(6), \
	 graduatedyear 	varchar(4), \
	 remark 	varchar(50), \
	 UPDATED	timestamp default current timestamp \
	)

alter table cultivate_dat add constraint pk_cultivate_dat primary key \
        (entey_year, entey_index)
