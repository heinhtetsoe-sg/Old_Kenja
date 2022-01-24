
drop table recordsemes_dat

create table recordsemes_dat \
      (copycd            varchar(1)      not null, \
       year              varchar(4)      not null, \
       semester          varchar(1)      not null, \
       
       testkindcd        varchar(2)      not null, \
       
       gradingclasscd    varchar(6)      not null, \
       schregno          varchar(8)      not null, \
       subclasscd        varchar(6), \
       chaircd           varchar(7), \
       old_tmpval        smallint, \
       new_tmpval        smallint, \
       mod_val           smallint, \
       educate_val       smallint, \
       valuation         smallint, \   
       registercd        varchar(8), \
       updated           timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table recordsemes_dat add constraint pk_recordsemes_dat primary key (copycd,year,semester,testkindcd,gradingclasscd,schregno)

