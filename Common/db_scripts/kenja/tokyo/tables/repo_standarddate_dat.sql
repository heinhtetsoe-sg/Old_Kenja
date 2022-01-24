--add m-yama

drop table repo_standarddate_dat

create table repo_standarddate_dat \
      (year             varchar(4)    not null, \
       standard_no      smallint      not null, \
       standard_seq     smallint      not null, \
       semester         varchar(1), \
       standard_date    date, \
       deadline_date    date, \
       registercd       varchar(8), \
       updated          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table repo_standarddate_dat add constraint pk_repo_stnddt_dt primary key \
      (year, standard_no, standard_seq)


