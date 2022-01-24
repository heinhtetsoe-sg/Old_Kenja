
drop table college_year_dat

create table college_year_dat \
	( \
	 year		varchar(4)	not null, \
	 school_cd 	varchar(8)	not null, \
	 juken_howto 	varchar(4)      not null, \
	 shinrono 	varchar(8), \
	 recommend 	varchar(80), \
	 UPDATED	timestamp default current timestamp \
	)

alter table college_year_dat add constraint pk_college_year primary key \
        (year, school_cd, juken_howto)
