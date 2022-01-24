drop table progress_dat


create table progress_dat \
        (progressyear             varchar(4)     not null, \
         schregno                 varchar(6)     not null, \
         shiftcd                  varchar(1)  , \
	 remaincredits            smallint    , \
         updated                  timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table progress_dat add constraint pk_prgrss_dt primary key \
         (progressyear, schregno)
