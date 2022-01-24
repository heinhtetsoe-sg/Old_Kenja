--2004/10/20 m-yama
--NO:001 update final1/final1 byte 2 from 3
--

drop table trend_final_dat

create table trend_final_dat \
	( \
	 year		varchar(4)	not null, \
	 schregno 	varchar(6)	not null, \
				 --001 вн
	 final1 	varchar(3), \
				 --001 вн
	 final2 	varchar(3), \
	 UPDATED		timestamp default current timestamp \
	)

alter table trend_final_dat add constraint pk_trend_final_dat primary key \
        (year, schregno)
