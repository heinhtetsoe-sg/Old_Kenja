
drop table recordsemes_hdat

create table recordsemes_hdat \
      (copycd            varchar(1)      not null, \
       year              varchar(4)      not null, \
       semester          varchar(1)      not null, \
       
       testkindcd        varchar(2)      not null, \
       
       chaircd           varchar(7)      not null, \
       avgmod_flg        varchar(1), \
       registercd        varchar(8), \
       updated           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table recordsemes_hdat add constraint pk_recsemes_hdat primary key (copycd,year,semester,testkindcd,chaircd)

