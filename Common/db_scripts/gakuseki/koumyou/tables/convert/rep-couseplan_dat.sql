--
--進路情報データ
--

drop table couseplan_dat_old

create table couseplan_dat_old like couseplan_dat

insert into couseplan_dat_old select * from couseplan_dat

drop table couseplan_dat

create table couseplan_dat \
	( \
	 YEAR	 		    varchar(4)	not null, \
	 INDEX	 		    integer	not null, \
	 SCHREGNO 		    varchar(6)	, \
	 STAT_KIND  	    varchar(1), \
	 SENKOU_KIND 	    varchar(1), \
	 STAT_CD    	    varchar(8), \
	 STAT_NAME  	    varchar(80), \
	 BUNAME     	    varchar(80), \
	 SCHOOL_SORT   	    varchar(1), \
	 TELNO      	    varchar(16), \
	 HOWTOEXAM 		    varchar(4), \
	 HOWTOEXAM_REMARK	varchar(80), \
	 HAND_DATE 		    date, \
	 DECISION 		    varchar(4), \
	 PLANSTAT 		    varchar(4), \
     PLANSTAT_FIN       varchar(1), \
	 PRINT_DATE         date, \
	 SENKOU_NO     	    dec(5,0), \
	 TOROKU_DATE	    date, \
	 JUKEN_HOWTO	    varchar(4), \
	 RECOMMEND     	    varchar(80), \
	 ATTEND     	    smallint, \
	 AVG        	    dec(2,1), \
	 TEST       	    dec(2,0), \
	 SEISEKI     	    dec(3,0), \
	 SENKOU_KAI 	    varchar(4), \
	 SENKOU_FIN 	    varchar(4), \
	 REMARK 	        varchar(40), \
	 STAT_DATE1 	    date, \
	 STAT_STEME 	    time, \
	 STAT_ETEME 	    time, \
     SHOZOKU            varchar(20), \
	 STAT_DATE2 	    date, \
	 CONTENTEXAM 	    varchar(80), \
	 REASONEXAM 	    varchar(162), \
	 THINKEXAM 		    varchar(326), \
	 JOB_DATE1 		    date, \
	 JOB_STEME 		    time, \
	 JOB_ETEME 		    time, \
     SHUSHOKU_ADD	    varchar(80), \
	 JOB_REMARK 	    varchar(80), \
	 JOB_CONTENT 	    varchar(162), \
	 JOB_THINK 		    varchar(326), \
	 JOBEX_DATE1 	    date, \
	 JOBEX_STEME 	    time, \
	 JOBEX_ETEME 	    time, \
	 JOBEX_REMARK 	    varchar(80), \
	 JOBEX_CONTENT 	    varchar(162), \
	 JOBEX_THINK 	    varchar(326), \
	 UPDATED        	timestamp default current timestamp \
	)

alter table couseplan_dat add constraint pk_couseplan_dat primary key \
        (year, index)

insert into couseplan_dat \
select \
	 YEAR	 		    , \
	 INDEX	 		    , \
	 SCHREGNO 		    , \
	 STAT_KIND  	    , \
	 SENKOU_KIND 	    , \
	 STAT_CD    	    , \
	 STAT_NAME  	    , \
	 BUNAME     	    , \
	 SCHOOL_SORT   	    , \
	 TELNO      	    , \
	 HOWTOEXAM 		    , \
	 HOWTOEXAM_REMARK	, \
	 HAND_DATE 		    , \
	 DECISION 		    , \
	 PLANSTAT 		    , \
     cast(null as varchar(1)), \
	 PRINT_DATE         , \
	 SENKOU_NO     	    , \
	 TOROKU_DATE	    , \
	 JUKEN_HOWTO	    , \
	 RECOMMEND     	    , \
	 ATTEND     	    , \
	 AVG        	    , \
	 TEST       	    , \
	 SEISEKI     	    , \
	 SENKOU_KAI 	    , \
	 SENKOU_FIN 	    , \
	 REMARK 	        , \
	 STAT_DATE1 	    , \
	 STAT_STEME 	    , \
	 STAT_ETEME 	    , \
     SHOZOKU            , \
	 STAT_DATE2 	    , \
	 CONTENTEXAM 	    , \
	 REASONEXAM 	    , \
	 THINKEXAM 		    , \
	 JOB_DATE1 		    , \
	 JOB_STEME 		    , \
	 JOB_ETEME 		    , \
     SHUSHOKU_ADD	    , \
	 JOB_REMARK 	    , \
	 JOB_CONTENT 	    , \
	 JOB_THINK 		    , \
	 JOBEX_DATE1 	    , \
	 JOBEX_STEME 	    , \
	 JOBEX_ETEME 	    , \
	 JOBEX_REMARK 	    , \
	 JOBEX_CONTENT 	    , \
	 JOBEX_THINK 	    , \
	 UPDATED        	  \
from couseplan_dat_old

