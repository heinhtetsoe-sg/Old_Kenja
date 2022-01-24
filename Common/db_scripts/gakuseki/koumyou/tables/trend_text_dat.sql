--2004/10/20 m-yama
--NO:001 ¹àÌÜÄÉ²Ã citem18¡Ácitem21
--NO:002 ¹àÌÜÄÉ²Ã csitem11¡Ácsitem14
--

drop table trend_text_dat

create table trend_text_dat \
	( \
	 year		varchar(4)	not null, \
	 item1 		varchar(40), \
	 item1_into 	varchar(80), \
	 item2 		varchar(40), \
	 item2_into 	varchar(80), \
	 item3 		varchar(40), \
	 item3_into 	varchar(80), \
	 item4 		varchar(40), \
	 item4_into 	varchar(80), \
	 item5 		varchar(40), \
	 item5_into 	varchar(80), \
	 item6 		varchar(40), \
	 item6_into 	varchar(80), \
	 item7 		varchar(40), \
	 item7_into 	varchar(80), \
	 item8 		varchar(40), \
	 item8_into 	varchar(80), \
	 item9 		varchar(40), \
	 item9_into 	varchar(80), \
	 item10 	varchar(40), \
	 item10_into 	varchar(80), \
	 item11 	varchar(40), \
	 item11_into 	varchar(80), \
	 item12 	varchar(40), \
	 item12_into 	varchar(80), \
	 item13 	varchar(40), \
	 item13_into 	varchar(80), \
	 kitem1 	varchar(40), \
	 kitem2 	varchar(40), \
	 kitem3 	varchar(40), \
	 kitem4 	varchar(40), \
	 kitem5 	varchar(40), \
	 kitem6 	varchar(40), \
	 kitem7 	varchar(40), \
	 citem1 	varchar(40), \
	 citem2 	varchar(40), \
	 citem3 	varchar(40), \
	 citem4 	varchar(40), \
	 citem5 	varchar(40), \
	 citem6 	varchar(40), \
	 citem7 	varchar(40), \
	 citem8 	varchar(40), \
	 citem9 	varchar(40), \
	 citem10 	varchar(40), \
	 citem11 	varchar(40), \
	 citem12 	varchar(40), \
	 citem13 	varchar(40), \
	 citem14 	varchar(40), \
	 citem15 	varchar(40), \
	 citem16 	varchar(40), \
	 citem17 	varchar(40), \
	-- 001 start¢­------------
	 citem18 	varchar(40), \
	 citem19 	varchar(40), \
	 citem20 	varchar(40), \
	 citem21 	varchar(40), \
	-- 001 end  ¢¬------------
	 citem22 	varchar(40), \
	 citem23 	varchar(40), \
	 citem24 	varchar(40), \
	 citem25 	varchar(40), \
	 citem26 	varchar(40), \
	 citem27 	varchar(40), \
	 citem28 	varchar(40), \
	 citem29 	varchar(40), \
	 citem30 	varchar(40), \
	 citem31 	varchar(40), \
	 citem32 	varchar(40), \
	 citem33 	varchar(40), \
	 citem34 	varchar(40), \
	 csitem1 	varchar(40), \
	 csitem1_into 	varchar(80), \
	 csitem2 	varchar(40), \
	 csitem2_into 	varchar(80), \
	 csitem3 	varchar(40), \
	 csitem3_into 	varchar(80), \
	 csitem4 	varchar(40), \
	 csitem4_into 	varchar(80), \
	 csitem5 	varchar(40), \
	 csitem5_into 	varchar(80), \
	 csitem6 	varchar(40), \
	 csitem6_into 	varchar(80), \
	 csitem7 	varchar(40), \
	 csitem7_into 	varchar(80), \
	 csitem8 	varchar(40), \
	 csitem8_into 	varchar(80), \
	 csitem9 	varchar(40), \
	 csitem9_into 	varchar(80), \
	 csitem10 	varchar(40), \
	 csitem10_into 	varchar(80), \
	-- 002 start¢­------------
	 csitem11 	varchar(40), \
	 csitem11_into 	varchar(80), \
	 csitem12 	varchar(40), \
	 csitem12_into 	varchar(80), \
	 csitem13 	varchar(40), \
	 csitem13_into 	varchar(80), \
	 csitem14 	varchar(40), \
	 csitem14_into 	varchar(80), \
	-- 002 end  ¢¬------------
	 UPDATED		timestamp default current timestamp \
	)

alter table trend_text_dat add constraint pk_trend_text_dat primary key \
        (year)
