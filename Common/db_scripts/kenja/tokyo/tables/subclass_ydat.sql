--standard_no ди╡ц 2004/12/01 m-yama

drop table subclass_ydat

create table subclass_ydat \
      (year             varchar(4)      not null, \
       subclasscd       varchar(6)      not null, \
       standard_no      smallint, \
       registercd       varchar(8), \
       updated          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table subclass_ydat add constraint pk_subclass_ydat primary key \
      (year, subclasscd)


