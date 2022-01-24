drop table sch_chr_dat

create table sch_chr_dat \
      (executedate        date         not null, \
       periodcd           varchar(1)   not null, \
       chaircd            varchar(7)   not null, \
       executed           varchar(1), \
       datadiv            varchar(1), \
       year               varchar(4), \
       semester           varchar(1), \
       attestor           varchar(8), \
       uncount            varchar(1), \
       registercd         varchar(8), \
       updated            timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table sch_chr_dat add constraint pk_sch_chr_dat primary key \
      (executedate,periodcd,chaircd)
