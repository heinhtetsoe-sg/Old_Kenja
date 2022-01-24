
drop table couseling_dat

create table couseling_dat \
	( \
	 YEAR 		varchar(4) NOT NULL, \
	 COUSELINO_INDEX	varchar(4) NOT NULL, \
	 REZADATE	DATE, \
	 REZATIME 	TIME, \
	 SCHREGNO 	varchar(6), \
	 REMARK 	varchar(166), \
	 UPDATED	timestamp default current timestamp \
	)

alter table couseling_dat add constraint pk_couseling_dat primary key \
        (year, couselino_index)
