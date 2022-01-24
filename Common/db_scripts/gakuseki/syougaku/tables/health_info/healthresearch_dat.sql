
drop table healthresearch_dat

create table healthresearch_dat \
	(schregno		varchar(6)	not null, \
	 res_year		varchar(4)	not null, \
	 res_date		date		not null, \
	 constitution		varchar(1), \
	 towhat			varchar(20), \
	 bedtime		varchar(5), \
	 risingtime		varchar(5), \
	 sleeping		varchar(1), \
	 toothbrushtimes	smallint, \
	 toothbruth1		varchar(1), \
	 toothbruch2		varchar(1), \
	 defecationday		smallint, \
	 defecationtimes	smallint, \
	 defecation		varchar(1), \
	 schoolwater		smallint, \
	 schooltea		smallint, \
	 schoolmilk		smallint, \
	 schooljuice		smallint, \
	 schoolcoke		smallint, \
	 schoolcoffeemilk	smallint, \
	 schoolyogurt		smallint, \
	 schoolother		smallint, \
	 homewater		smallint, \
	 hometea		smallint, \
	 homemilk		smallint, \
	 homejuice		smallint, \
	 homecoke		smallint, \
	 homecoffeemilk		smallint, \
	 homeyogult		smallint, \
	 homeother		smallint, \
	 breakfasttime		varchar(5), \
	 breakfaststatus	varchar(1), \
	 breakfastreason	varchar(20), \
	 lunchtime		varchar(5), \
	 lunchstatus		varchar(1), \
	 lunch			varchar(1), \
	 lunchother		varchar(20), \
	 dinnertime		varchar(5), \
	 dinnerstatus		varchar(1), \
	 dinnerreason		varchar(20), \
	 snacktimes		smallint, \
	 snacktime		varchar(1), \
	 snack			varchar(20), \
	 dislikestatus		varchar(1), \
	 dislikes		varchar(20), \
	 heartdisease		varchar(1), \
	 heartdiseaseage	smallint, \
	 kidneydisease		varchar(1), \
	 kidneydiseaseage	smallint, \
	 liverdisease		varchar(1), \
	 liverdiseaseage	smallint, \
	 asthma			varchar(1), \
	 asthmaage		smallint, \
	 otitismedia		varchar(1), \
	 otitismediaage		smallint, \
	 rhinitis		varchar(1), \
	 rhinitisage		smallint, \
	 gastritis		varchar(1), \
	 gastritisage		smallint, \
	 empyema		varchar(1), \
	 empyemaage		smallint, \
	 tonsillitis		varchar(1), \
	 tonsillitisage		smallint, \
	 rheumaticfever		varchar(1), \
	 rheumaticfeverage	smallint, \
	 spasm			varchar(1), \
	 spasmage		smallint, \
	 other			varchar(1), \
	 otherage		smallint, \
	 wound			varchar(1), \
	 woundage		smallint, \
	 trafficaccident	varchar(1), \
	 trafficaccidentage	smallint, \
	 noparticular		varchar(1), \
	 currentcondition	varchar(30), \
	 noticetoschool		varchar(30), \
	 counselcontent		varchar(30), \
	 currentdisease		varchar(30), \
	 updated		timestamp default current timestamp \
	) in usr1dms index in idx1dms

alter table healthresearch_dat add constraint pk_heares_dat primary key \
	(schregno, res_year, res_date)


