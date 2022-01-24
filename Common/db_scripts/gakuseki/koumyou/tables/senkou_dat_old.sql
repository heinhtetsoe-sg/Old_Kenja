
drop table senkou_dat

create table senkou_dat \
	( \
	 year	 	varchar(4)	not null, \
	 toroku_date 	date		not null, \
	 schregno 	varchar(6)	not null, \
	 senkou_kind 	varchar(1), \
	 senkou_cd 	varchar(8), \
	 senkou_name 	varchar(80), \
	 buname     	varchar(80), \
	 juken_howto   	varchar(4), \
	 school_sort   	varchar(1), \
	 recommend     	varchar(80), \
	 attend     	smallint, \
	 avg        	dec(2,1), \
	 test       	dec(2,0), \
	 seiseki     	dec(3,0), \
	 senkou_kai 	varchar(4), \
	 senkou_fin 	varchar(4), \
	 remark 	varchar(40), \
	 UPDATED	timestamp default current timestamp \
	)

alter table senkou_dat add constraint pk_senkou_dat primary key \
        (year, toroku_date, schregno)
